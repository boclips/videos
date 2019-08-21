package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.IndexConfiguration.Companion.unstemmed
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.Fuzziness
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
        val query = if (isIdLookup(videoQuery)) lookUpById(videoQuery.ids) else findBySearchTerm(videoQuery)
        val request = SearchRequest(
            arrayOf(VideosIndex.getIndexAlias()),
            query.from(startIndex).size(windowSize)
        )

        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun findBySearchTerm(videoQuery: VideoQuery): SearchSourceBuilder {
        val phrase = videoQuery.phrase
        val query = QueryBuilders.boolQuery()

        if (phrase.isNotBlank()) {
            query
                .should(QueryBuilders.matchPhraseQuery(VideoDocument.TITLE, phrase).slop(1))
                .should(QueryBuilders.matchPhraseQuery(VideoDocument.DESCRIPTION, phrase).slop(1))
                .should(
                    QueryBuilders.multiMatchQuery(phrase,
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
                .should(QueryBuilders.termQuery(VideoDocument.CONTENT_PROVIDER, phrase).boost(1000F))
                .should(QueryBuilders.matchPhraseQuery(VideoDocument.SUBJECTS,phrase).boost(1000F))
                .minimumShouldMatch(1)
        }

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

    private fun isIdLookup(videoQuery: VideoQuery) = videoQuery.ids.isNotEmpty()
}
