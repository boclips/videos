package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.SuggestionRequest
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.domain.search.SearchSuggestionsResults

class FakeSuggestionsIndex<SUGGESTION_QUERY : SearchQuery<METADATA>, METADATA> {
    private val index = mutableMapOf<String, METADATA>()
    private var requests: MutableList<SuggestionRequest<SUGGESTION_QUERY>> = mutableListOf()

    fun getSuggestions(
        suggestionRequest: SuggestionRequest<SUGGESTION_QUERY>,
        filter: (index: MutableMap<String, METADATA>, query: SUGGESTION_QUERY) -> List<Suggestion>
    ): SearchSuggestionsResults {
        requests.add(suggestionRequest)

        val matchingSuggestions = filter(index, suggestionRequest.query)

        return SearchSuggestionsResults(
            elements = matchingSuggestions
        )
    }

    fun upsert(
        items: Sequence<METADATA>,
        transformMetadata: (item: METADATA) -> Pair<String, METADATA>,
        notifier: ProgressNotifier?
    ) {
        items.forEach { item ->
            val pair = transformMetadata(item)
            index[pair.first] = pair.second
        }

        notifier?.complete()
    }

    fun safeRebuildIndex(
        items: Sequence<METADATA>,
        transformMetadata: (item: METADATA) -> Pair<String, METADATA>,
        notifier: ProgressNotifier?
    ) {
        clear()
        upsert(items, transformMetadata, notifier)
    }

    fun removeFromSearch(itemId: String) {
        index.remove(itemId)
    }

    fun bulkRemoveFromSearch(itemIds: List<String>) {
        itemIds.forEach(this::removeFromSearch)
    }

    fun clear() {
        index.clear()
    }
}
