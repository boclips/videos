package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.common.Do
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.infrastructure.IndexConfiguration.Companion.unstemmed
import com.boclips.search.service.infrastructure.common.FilterDecorator
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
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
import org.elasticsearch.index.query.QueryBuilders.termsQuery
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder as EsSortOrder

class VideoIndexReader(val client: RestHighLevelClient) : IndexReader<VideoMetadata, VideoQuery> {
    companion object : KLogging();

    override fun search(searchRequest: PaginatedSearchRequest<VideoQuery>): List<String> {
        return search(searchRequest.query, searchRequest.startIndex, searchRequest.windowSize)
            .map(VideoDocumentConverter::fromSearchHit)
            .map { it.id }
    }

    override fun count(query: VideoQuery): Long {
        return search(videoQuery = query, startIndex = 0, windowSize = 1).totalHits?.value ?: 0L
    }

    private fun search(videoQuery: VideoQuery, startIndex: Int, windowSize: Int): SearchHits {
        val query = buildElasticSearchQuery(videoQuery)
        val request = SearchRequest(
            arrayOf(VideosIndex.getIndexAlias()),
            query.from(startIndex).size(windowSize).explain(false)
        )
        val result = client.search(request, RequestOptions.DEFAULT)
        return result.hits
    }

    private fun buildElasticSearchQuery(videoQuery: VideoQuery): SearchSourceBuilder {
        val phrase = videoQuery.phrase
        val query = boolQuery()

        query
            .apply {
                if (phrase.isNotBlank()) {
                    must(
                        boolQuery()
                            .should(matchPhraseQuery(VideoDocument.TITLE, phrase).slop(1))
                            .should(matchPhraseQuery(VideoDocument.DESCRIPTION, phrase).slop(1))
                            .should(
                                multiMatchQuery(
                                    phrase,
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
                            .should(termQuery(VideoDocument.CONTENT_PROVIDER, phrase).boost(1000F))
                            .should(matchPhraseQuery(VideoDocument.SUBJECT_NAMES, phrase).boost(1000F))
                            .minimumShouldMatch(1)
                            .let(boostInstructionalVideos())
                            .let(boostWhenSubjectsMatch(videoQuery.userSubjectIds))
                    )
                }
            }.apply {
                permittedIdsFilter(this, videoQuery.ids, videoQuery.permittedVideoIds)
            }

        FilterDecorator(query).apply(videoQuery)

        val esQuery = SearchSourceBuilder().query(query)

        videoQuery.sort?.let { sort ->
            Do exhaustive when (sort) {
                is Sort.ByField -> esQuery.sort(sort.fieldName.name, EsSortOrder.fromString(sort.order.toString()))
                is Sort.ByRandom -> esQuery.query(
                    FunctionScoreQueryBuilder(esQuery.query(), ScoreFunctionBuilders.randomFunction()).boostMode(
                        CombineFunction.REPLACE
                    )
                )
            }
        }

        return esQuery
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
