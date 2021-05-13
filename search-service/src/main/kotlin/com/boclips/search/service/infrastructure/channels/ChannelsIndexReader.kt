package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ChannelAccessRuleQuery
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.SearchResults
import com.boclips.search.service.domain.common.model.IndexSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SuggestionRequest
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.domain.common.suggestions.SuggestionsIndexReader
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
    SuggestionsIndexReader<ChannelMetadata, SuggestionQuery<ChannelMetadata>>,
    IndexReader<ChannelMetadata, ChannelQuery> {
    companion object : KLogging() {
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

    override fun getSuggestions(suggestionRequest: SuggestionRequest<SuggestionQuery<ChannelMetadata>>): SearchSuggestionsResults {
        val results = searchQuery(
            ChannelQuery(
                phrase = suggestionRequest.query.phrase,
                accessRuleQuery = suggestionRequest.query.accessRuleQuery?.let {
                    ChannelAccessRuleQuery(
                        excludedContentPartnerIds = it.excludedContentPartnerIds,
                        includedChannelIds = it.includedChannelIds,
                        includedTypes = it.includedTypes,
                        excludedTypes = it.excludedTypes,
                        isEligibleForStream = it.isEligibleForStream,
                    )
                },
                sort = suggestionRequest.query.sort
            )
        )

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

    override fun search(searchRequest: IndexSearchRequest<ChannelQuery>): SearchResults {
        TODO("Not yet implemented")
    }

    private fun searchQuery(channelQuery: ChannelQuery): SearchHits {
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

    private fun buildSort(channelQuery: ChannelQuery): SortBuilder<*>? {
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
