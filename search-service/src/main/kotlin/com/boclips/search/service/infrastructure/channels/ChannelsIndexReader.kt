package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.model.ChannelsSearchRequest
import com.boclips.search.service.domain.common.model.SuggestionsSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.suggestions.IndexReader
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.domain.search.SearchSuggestionsResults
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.script.Script
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.ScriptSortBuilder
import org.elasticsearch.search.sort.SortBuilder
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import com.boclips.search.service.domain.common.model.SortOrder as BoclipsSortOrder

class ChannelsIndexReader(val client: RestHighLevelClient) :
    IndexReader<ChannelMetadata, SuggestionQuery<ChannelMetadata>> {
    companion object : KLogging() {

        private val SUGGESTION_PAGE_NUMBER_DEFAULT = 0
        private val SUGGESTION_PAGE_SIZE_DEFAULT = 10

        private val sortByTaxonomyScript = Script(
            """
                         if(doc['taxonomyVideoLevelTagging'].value) {
                             return '01 needs video tagging';
                         } else if (!doc.containsKey('taxonomyCategories') || doc['taxonomyCategories'].size() == 0) {
                             return '00 untagged';
                         } else {
                             return doc['taxonomyCategories'][0];
                         }
                    """
        )
    }

    private val elasticSearchResultConverter = ChannelsDocumentConverter()

    override fun search(searchRequest: SuggestionsSearchRequest<SuggestionQuery<ChannelMetadata>>): SearchSuggestionsResults {
        val results = searchQuery(searchRequest)

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

     fun searchChannels(searchRequest: ChannelsSearchRequest): SearchSuggestionsResults {
        val results = searchQuery(searchRequest)

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

    private fun searchQuery(searchRequest: SuggestionsSearchRequest<SuggestionQuery<ChannelMetadata>>): SearchHits {
        val query = SearchSourceBuilder().apply {
            query(ChannelEsQuery().mainQuery(searchRequest.query))
            buildSort(searchRequest.query)?.let { sort(it) }
        }

        val request = SearchRequest(
            arrayOf(ChannelsIndex.getIndexAlias()),
            query.from(SUGGESTION_PAGE_NUMBER_DEFAULT).size(SUGGESTION_PAGE_SIZE_DEFAULT)
        )

        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun searchQuery(searchRequest: ChannelsSearchRequest): SearchHits {

        val paginationSize = searchRequest.pagination?.pageSize ?: SUGGESTION_PAGE_SIZE_DEFAULT
        val paginationStartIndex = searchRequest.pagination?.let {
            (searchRequest.pagination.pageNumber - 1) * searchRequest.pagination.pageSize
        } ?: SUGGESTION_PAGE_NUMBER_DEFAULT

        val request = SearchRequest(
            arrayOf(ChannelsIndex.getIndexAlias()),
            SearchSourceBuilder().from(paginationStartIndex).size(paginationSize)
        )

        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun buildSort(channelQuery: SuggestionQuery<ChannelMetadata>): SortBuilder<*>? {
        return channelQuery.sort
            .find { (it as? Sort.ByField)?.fieldName == ChannelMetadata::taxonomy }
            ?.let { taxonomySortMetadata ->
                SortBuilders
                    .scriptSort(sortByTaxonomyScript, ScriptSortBuilder.ScriptSortType.STRING)
                    .order((taxonomySortMetadata as Sort.ByField).toElasticsearchOrder())
            }
    }

    private fun Sort.ByField<ChannelMetadata>.toElasticsearchOrder() =
        when (order) {
            BoclipsSortOrder.DESC -> SortOrder.DESC
            BoclipsSortOrder.ASC -> SortOrder.ASC
        }
}
