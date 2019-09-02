package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.infrastructure.IndexConfiguration.Companion.unstemmed
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery
import org.elasticsearch.index.query.QueryBuilders.multiMatchQuery
import org.elasticsearch.index.query.QueryBuilders.termQuery
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder

class VideoIndexReader(val client: RestHighLevelClient) : IndexReader<VideoMetadata, VideoQuery> {
    companion object : KLogging();

    override fun search(searchRequest: PaginatedSearchRequest<VideoQuery>): List<String> {
        return search(searchRequest.query, searchRequest.startIndex, searchRequest.windowSize)
            .map(VideoDocumentConverter::fromSearchHit)
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
        val query = boolQuery()

        if (phrase.isNotBlank()) {
            query.must(
                QueryBuilders.boostingQuery(
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
                        .minimumShouldMatch(1),
                    boolQuery().mustNot(termQuery(VideoDocument.TYPE, VideoType.INSTRUCTIONAL.name))
                ).negativeBoost(0.8F)
            )
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
        val lookUpByIdQuery = boolQuery().should(bunchOfIds)
        return SearchSourceBuilder().query(lookUpByIdQuery)
    }

    private fun isIdLookup(videoQuery: VideoQuery) = videoQuery.ids.isNotEmpty()
}
