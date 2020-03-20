package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.common.Do
import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.Counts
import com.boclips.search.service.domain.common.FilterCounts
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.infrastructure.IndexConfiguration.Companion.unstemmed
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.lucene.search.function.CombineFunction
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.boostingQuery
import org.elasticsearch.index.query.QueryBuilders.idsQuery
import org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery
import org.elasticsearch.index.query.QueryBuilders.multiMatchQuery
import org.elasticsearch.index.query.QueryBuilders.termQuery
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder as EsSortOrder

class VideoIndexReader(val client: RestHighLevelClient) : IndexReader<VideoMetadata, VideoQuery> {
    companion object : KLogging();

    override fun search(searchRequest: PaginatedSearchRequest<VideoQuery>): List<String> {
        return search(searchRequest.query, searchRequest.startIndex, searchRequest.windowSize).hits
            .map(VideoDocumentConverter::fromSearchHit)
            .map { it.id }
    }

    override fun count(query: VideoQuery): Counts {
        val response = search(videoQuery = query, startIndex = 0, windowSize = 1)
        val subjectCounts = response
            .aggregations.get<ParsedFilter>("subjects")
            .aggregations.get<ParsedStringTerms>("subject ids")
            .buckets.map { bucket ->
            Count(id = bucket.key.toString(), hits = bucket.docCount)
        }
        return Counts(hits = response.hits.totalHits?.value ?: 0L, buckets = FilterCounts(subjects = subjectCounts))
    }

    private fun search(videoQuery: VideoQuery, startIndex: Int, windowSize: Int): SearchResponse {
        val query = buildElasticSearchQuery(videoQuery)
        val request = SearchRequest(
            arrayOf(VideosIndex.getIndexAlias()),
            query.from(startIndex).size(windowSize).explain(false)
        )
        return client.search(request, RequestOptions.DEFAULT)
    }

    private fun buildElasticSearchQuery(videoQuery: VideoQuery): SearchSourceBuilder {
        val query = query(videoQuery)
        val queryFilters = VideoFilter().create(videoQuery)
        val aggregationFilters = VideoFilter().createAggregationFilter(videoQuery)

        val subjectAggregation = AggregationBuilders
            .filter("subjects", aggregationFilters)
            .subAggregation(AggregationBuilders.terms("subject ids").field(VideoDocument.SUBJECT_IDS))

        val searchSourceBuilder: SearchSourceBuilder = SearchSourceBuilder()
            .query(query)
            .aggregation(subjectAggregation)
            .postFilter(queryFilters)

        videoQuery.sort?.let { sort: Sort<VideoMetadata> ->
            Do exhaustive when (sort) {
                is Sort.ByField -> searchSourceBuilder.sort(
                    sort.fieldName.name,
                    EsSortOrder.fromString(sort.order.toString())
                )
                is Sort.ByRandom -> searchSourceBuilder.query(
                    FunctionScoreQueryBuilder(
                        searchSourceBuilder.query(),
                        ScoreFunctionBuilders.randomFunction()
                    ).boostMode(
                        CombineFunction.REPLACE
                    )
                )
            }
        }

        return searchSourceBuilder
    }

    private fun query(videoQuery: VideoQuery): BoolQueryBuilder? {
        val query = boolQuery()
        query
            .apply {
                if (videoQuery.phrase.isNotBlank()) {
                    must(
                        boolQuery()
                            .should(matchPhraseQuery(VideoDocument.TITLE, videoQuery.phrase).slop(1))
                            .should(matchPhraseQuery(VideoDocument.DESCRIPTION, videoQuery.phrase).slop(1))
                            .should(
                                multiMatchQuery(
                                    videoQuery.phrase,
                                    VideoDocument.TITLE,
                                    unstemmed(VideoDocument.TITLE),
                                    VideoDocument.DESCRIPTION,
                                    unstemmed(VideoDocument.DESCRIPTION),
                                    VideoDocument.TRANSCRIPT,
                                    unstemmed(VideoDocument.TRANSCRIPT),
                                    VideoDocument.KEYWORDS
                                )
                                    .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
                                    .minimumShouldMatch("75%")
                                    .fuzziness(Fuzziness.ZERO)
                            )
                            .should(termQuery(VideoDocument.CONTENT_PROVIDER, videoQuery.phrase).boost(1000F))
                            .should(matchPhraseQuery(VideoDocument.SUBJECT_NAMES, videoQuery.phrase).boost(1000F))
                            .minimumShouldMatch(1)
                            .let(boostInstructionalVideos())
                            .let(boostWhenSubjectsMatch(videoQuery.userSubjectIds))
                    )
                }
            }.apply {
                permittedIdsFilter(this, videoQuery.ids, videoQuery.permittedVideoIds)
            }
        return query
    }

    private fun permittedIdsFilter(
        currentQueryBuilder: BoolQueryBuilder,
        idsToLookup: Collection<String>,
        permittedVideoIds: Collection<String>?
    ): BoolQueryBuilder {
        val ids = permittedVideoIds
            ?.takeUnless { it.isNullOrEmpty() }
            ?.let {
                if (idsToLookup.isNotEmpty()) {
                    idsToLookup.intersect(permittedVideoIds)
                } else {
                    permittedVideoIds
                }
            }
            ?: idsToLookup

        if (ids.isNotEmpty()) {
            currentQueryBuilder.must(
                boolQuery().must(
                    idsQuery().addIds(*(ids.toTypedArray()))
                )
            )
        }

        return currentQueryBuilder
    }

    private fun boostInstructionalVideos() = { innerQuery: QueryBuilder ->
        boostingQuery(
            innerQuery,
            boolQuery().mustNot(termQuery(VideoDocument.TYPE, VideoType.INSTRUCTIONAL.name))
        ).negativeBoost(0.4F)
    }

    private fun boostWhenSubjectsMatch(subjectIds: Set<String>) = { innerQuery: QueryBuilder ->
        boostingQuery(
            innerQuery,
            subjectIds.fold(
                boolQuery(),
                { q, subjectId -> q.mustNot(matchPhraseQuery(VideoDocument.SUBJECT_IDS, subjectId)) })
        ).negativeBoost(0.5F)
    }
}
