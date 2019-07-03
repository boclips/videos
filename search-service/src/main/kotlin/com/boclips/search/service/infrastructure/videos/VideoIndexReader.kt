package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.IndexConfiguration.Companion.FIELD_DESCRIPTOR_SHINGLES
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchPhraseQueryBuilder
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.rescore.QueryRescoreMode
import org.elasticsearch.search.rescore.QueryRescorerBuilder
import org.elasticsearch.search.sort.SortOrder

class VideoIndexReader(val client: RestHighLevelClient) :
    IndexReader<VideoMetadata, VideoQuery> {
    companion object : KLogging();

    private val elasticSearchResultConverter = VideoDocumentConverter()

    override fun search(searchRequest: PaginatedSearchRequest<VideoQuery>): List<String> {
        return search(searchRequest.query, searchRequest.startIndex, searchRequest.windowSize)
            .map(elasticSearchResultConverter::convert)
            .map { it.id }
    }

    override fun count(query: VideoQuery): Long {
        return search(videoQuery = query, startIndex = 0, windowSize = 1).totalHits
    }

    private fun search(videoQuery: VideoQuery, startIndex: Int, windowSize: Int): SearchHits {
        val esQuery = if (isIdLookup(videoQuery)) {
            lookUpById(videoQuery.ids)
        } else {
            findBySearchTerm(videoQuery)
        }

        val request = SearchRequest(
            arrayOf(VideosIndex.getIndexAlias()),
            esQuery
                .from(startIndex)
                .size(windowSize)
        )

        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun findBySearchTerm(videoQuery: VideoQuery): SearchSourceBuilder {
        val mainQuery = SearchSourceBuilder().query(mainQuery(videoQuery))

        if (videoQuery.sort === null) {
            mainQuery.addRescorer(rescorer(videoQuery.phrase))
        } else {
            mainQuery.sort(videoQuery.sort.fieldName.name, SortOrder.fromString(videoQuery.sort.order.toString()))
        }

        return mainQuery
    }

    private fun lookUpById(ids: List<String>): SearchSourceBuilder {
        val bunchOfIds = QueryBuilders.idsQuery().addIds(*(ids.toTypedArray()))
        val lookUpByIdQuery = QueryBuilders.boolQuery().should(bunchOfIds)
        return SearchSourceBuilder().query(lookUpByIdQuery)
    }

    private fun mainQuery(videoQuery: VideoQuery): BoolQueryBuilder? {
        return QueryBuilders
            .boolQuery()
            .apply {
                should(matchContentPartnerExactly(videoQuery).boost(1000.0F))

                if (videoQuery.phrase.isEmpty()) {
                    must(matchFieldsFuzzy(videoQuery))
                }

                if (videoQuery.phrase.isNotEmpty()) {
                    should(matchFieldsFuzzy(videoQuery))
                }
            }
    }

    private fun matchContentPartnerExactly(videoQuery: VideoQuery): BoolQueryBuilder {
        return QueryBuilders
            .boolQuery()
            .apply {
                must(QueryBuilders.termQuery(VideoDocument.CONTENT_PROVIDER, videoQuery.phrase))

                FilterDecorator(this).apply(videoQuery)

            }
    }

    private fun matchFieldsFuzzy(videoQuery: VideoQuery): BoolQueryBuilder {
        return QueryBuilders
            .boolQuery()
            .apply {
                if (videoQuery.phrase.isNotEmpty()) {
                    must(matchTitleDescriptionKeyword(videoQuery.phrase))
                    should(boostTitleMatch(videoQuery.phrase))
                    should(boostDescriptionMatch(videoQuery.phrase))
                }

                FilterDecorator(this).apply(videoQuery)
            }
    }

    private fun boostDescriptionMatch(phrase: String?): MatchPhraseQueryBuilder {
        return QueryBuilders.matchPhraseQuery(VideoDocument.DESCRIPTION, phrase)
    }

    private fun boostTitleMatch(phrase: String?): MatchPhraseQueryBuilder {
        return QueryBuilders.matchPhraseQuery(VideoDocument.TITLE, phrase)
    }

    private fun matchTitleDescriptionKeyword(phrase: String?): MultiMatchQueryBuilder {
        return QueryBuilders
            .multiMatchQuery(
                phrase,
                VideoDocument.TITLE,
                "${VideoDocument.TITLE}.std",
                VideoDocument.DESCRIPTION,
                "${VideoDocument.DESCRIPTION}.std",
                VideoDocument.TRANSCRIPT,
                VideoDocument.KEYWORDS
            )
            .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
            .minimumShouldMatch("75%")
            .fuzziness(Fuzziness.ZERO)
    }

    private fun rescorer(phrase: String?): QueryRescorerBuilder {
        val rescoreQuery = QueryBuilders.multiMatchQuery(
            phrase,
            "title.$FIELD_DESCRIPTOR_SHINGLES",
            "description.$FIELD_DESCRIPTOR_SHINGLES"
        )
            .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
        return QueryRescorerBuilder(rescoreQuery)
            .windowSize(100)
            .setScoreMode(QueryRescoreMode.Total)
    }

    private fun isIdLookup(videoQuery: VideoQuery) = videoQuery.ids.isNotEmpty()
}
