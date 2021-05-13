package com.boclips.search.service.infrastructure.common.suggestions

import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.SuggestionRequest
import com.boclips.search.service.domain.common.suggestions.SuggestionsIndexReader
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.domain.search.SearchSuggestionsResults

abstract class AbstractInMemoryFakeSuggestions<QUERY : SearchQuery<METADATA>, METADATA> :
    SuggestionsIndexReader<METADATA, QUERY>,
    IndexWriter<METADATA> {
    private val index = mutableMapOf<String, METADATA>()
    private var requests: MutableList<SuggestionRequest<QUERY>> = mutableListOf()

    override fun getSuggestions(suggestionRequest: SuggestionRequest<QUERY>): SearchSuggestionsResults {
        requests.add(suggestionRequest)

        val nameMatching = nameMatching(index, suggestionRequest.query)

        return SearchSuggestionsResults(
            elements = nameMatching.map { it ->
                Suggestion(
                    id = it.id,
                    name = it.name
                )
            }
        )
    }

    override fun upsert(items: Sequence<METADATA>, notifier: ProgressNotifier?) {
        items.forEach { channel ->
            upsertMetadata(index, channel)
        }
    }

    override fun safeRebuildIndex(items: Sequence<METADATA>, notifier: ProgressNotifier?) {
        index.clear()
        upsert(items, notifier)
    }
    override fun removeFromSearch(itemId: String) {
        index.remove(itemId)
    }

    override fun bulkRemoveFromSearch(itemIds: List<String>) {
        itemIds.forEach(this::removeFromSearch)
    }

    override fun makeSureIndexIsThere() {
    }

    fun clear() {
        index.clear()
    }

    abstract fun nameMatching(index: MutableMap<String, METADATA>, query: QUERY): List<Suggestion>
    abstract fun upsertMetadata(index: MutableMap<String, METADATA>, item: METADATA)
}
