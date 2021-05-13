package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.model.SearchRequestWithoutPagination
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
        private val sortByTaxonomyScript = Script(
            """
                         if(doc['taxonomyVideoLevelTagging'].value) {
                             return '1';
                         } else if (!doc.containsKey('taxonomyCategories') || doc['taxonomyCategories'].size() == 0) {
                             return '0'; // untagged - highest priority
                         } else {
                             return doc['taxonomyCategories'].stream().sorted().findFirst().get();
                         }
                    """
        )
    }

    private val elasticSearchResultConverter = ChannelsDocumentConverter()

    override fun search(searchRequest: SearchRequestWithoutPagination<SuggestionQuery<ChannelMetadata>>): SearchSuggestionsResults {
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

    private fun searchQuery(channelQuery: SuggestionQuery<ChannelMetadata>): SearchHits {
        val query = SearchSourceBuilder().apply {
            query(ChannelEsQuery().mainQuery(channelQuery))
            buildSort(channelQuery)?.let { sort(it) }
        }

        val request = SearchRequest(
            arrayOf(ChannelsIndex.getIndexAlias()),
            query.from(0).size(10)
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
