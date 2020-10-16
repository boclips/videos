package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.common.suggestions.IndexReader
import com.boclips.search.service.domain.search.SearchSuggestionsResults
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.domain.common.model.SearchRequestWithoutPagination
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder

class ChannelsIndexReader(val client: RestHighLevelClient) : IndexReader<ChannelMetadata, ChannelQuery> {
    companion object : KLogging()

    private val elasticSearchResultConverter = ChannelsDocumentConverter()

    override fun search(searchRequest: SearchRequestWithoutPagination<ChannelQuery>): SearchSuggestionsResults {
        val results = searchQuery(searchRequest.query)

        val elements = results
            .map(elasticSearchResultConverter::convert)
            .map {
                Suggestion(
                    id = it.id,
                    name = it.name
                )
            }

        return SearchSuggestionsResults(elements = elements)
    }

    private fun searchQuery(channelQuery: ChannelQuery): SearchHits {
        val query = SearchSourceBuilder().apply {
            query(ChannelEsQuery().mainQuery(channelQuery))
        }

        val request = SearchRequest(
            arrayOf(ChannelsIndex.getIndexAlias()),
            query.from(0).size(10)
        )

        return client.search(request, RequestOptions.DEFAULT).hits
    }
}
