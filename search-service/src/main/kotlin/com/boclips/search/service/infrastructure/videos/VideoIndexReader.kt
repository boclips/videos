package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.IndexConfiguration
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
import org.elasticsearch.search.sort.SortOrder

class VideoIndexReader(val client: RestHighLevelClient) : IndexReader<VideoMetadata, VideoQuery> {
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
                .explain(true)
        )

        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun findBySearchTerm(videoQuery: VideoQuery): SearchSourceBuilder {
        val query = mainQuery(videoQuery.phrase)

        FilterDecorator(query).apply(videoQuery)

        val esQuery = SearchSourceBuilder().query(query)

        videoQuery.sort?.let { sort ->
            esQuery.sort(sort.fieldName.name, SortOrder.fromString(sort.order.toString()))
        }

        return esQuery
    }

    private fun lookUpById(ids: List<String>): SearchSourceBuilder {
        val bunchOfIds = QueryBuilders.idsQuery().addIds(*(ids.toTypedArray()))
        val lookUpByIdQuery = QueryBuilders.boolQuery().should(bunchOfIds)
        return SearchSourceBuilder().query(lookUpByIdQuery)
    }

    private fun mainQuery(phrase: String): BoolQueryBuilder {
        val boolQuery = QueryBuilders.boolQuery()

        if(phrase.isBlank()) {
            return boolQuery
        }

        return boolQuery
            .should(matchTitleDescriptionKeyword(phrase))
            .should(boostTitleMatch(phrase))
            .should(boostDescriptionMatch(phrase))
            .should(QueryBuilders.termQuery(VideoDocument.CONTENT_PROVIDER, phrase).boost(1000F))
            .minimumShouldMatch(1)
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
                "${VideoDocument.TITLE}.${IndexConfiguration.FIELD_DESCRIPTOR_UNSTEMMED}",
                VideoDocument.DESCRIPTION,
                "${VideoDocument.DESCRIPTION}.${IndexConfiguration.FIELD_DESCRIPTOR_UNSTEMMED}",
                VideoDocument.TRANSCRIPT,
                VideoDocument.KEYWORDS
            )
            .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
            .minimumShouldMatch("75%")
            .fuzziness(Fuzziness.ZERO)
    }

    private fun isIdLookup(videoQuery: VideoQuery) = videoQuery.ids.isNotEmpty()
}
