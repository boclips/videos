package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.IndexReader
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.ResultCounts
import com.boclips.search.service.domain.common.SearchResults
import com.boclips.search.service.domain.common.model.SearchRequestWithoutPagination
import com.boclips.search.service.infrastructure.collections.CollectionEsQuery
import com.boclips.search.service.infrastructure.collections.CollectionFilterCriteria
import com.boclips.search.service.infrastructure.collections.CollectionsIndex
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder

class ChannelsIndexReader(val client: RestHighLevelClient) : IndexReader<ChannelMetadata, ChannelQuery> {
    companion object : KLogging();

    private val elasticSearchResultConverter = ChannelsDocumentConverter()

    override fun search(searchRequest: SearchRequestWithoutPagination<ChannelQuery>): SearchResults {
        val results = searchQuery(searchRequest.query)

        val elements = results
            .map(elasticSearchResultConverter::convert)
            .map { it.id }

        return SearchResults(elements = elements, counts = ResultCounts(totalHits = results.totalHits?.value ?: 0L))
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