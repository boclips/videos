package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.SearchResults
import com.boclips.search.service.domain.common.model.IndexSearchRequest
import com.boclips.search.service.domain.common.model.SuggestionRequest
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.domain.common.suggestions.SuggestionsIndexReader
import com.boclips.search.service.domain.search.SearchSuggestionsResults

class ChannelIndexFake :
    SuggestionsIndexReader<ChannelMetadata, SuggestionQuery<ChannelMetadata>>,
    IndexReader<ChannelMetadata, ChannelQuery>,
    IndexWriter<ChannelMetadata> {
    private val fakeSearchIndex = FakeSearchIndex<ChannelQuery, ChannelMetadata>()
    private val fakeSuggestionIndex = FakeSuggestionsIndex<SuggestionQuery<ChannelMetadata>, ChannelMetadata>()

    override fun search(searchRequest: IndexSearchRequest<ChannelQuery>): SearchResults {
        return fakeSearchIndex.search(searchRequest, this::performSearch)
    }

    override fun safeRebuildIndex(items: Sequence<ChannelMetadata>, notifier: ProgressNotifier?) {
        fakeSearchIndex.safeRebuildIndex(items, this::transformMetadata, notifier)
        fakeSuggestionIndex.safeRebuildIndex(items, this::transformMetadata, notifier)
    }

    override fun upsert(items: Sequence<ChannelMetadata>, notifier: ProgressNotifier?) {
        fakeSearchIndex.upsert(items, this::transformMetadata, notifier)
        fakeSuggestionIndex.upsert(items, this::transformMetadata, notifier)
    }

    override fun removeFromSearch(itemId: String) {
        fakeSearchIndex.removeFromSearch(itemId)
        fakeSuggestionIndex.removeFromSearch(itemId)
    }

    override fun bulkRemoveFromSearch(itemIds: List<String>) {
        fakeSearchIndex.bulkRemoveFromSearch(itemIds)
        fakeSuggestionIndex.bulkRemoveFromSearch(itemIds)
    }

    override fun makeSureIndexIsThere() {
    }

    override fun getSuggestions(suggestionRequest: SuggestionRequest<SuggestionQuery<ChannelMetadata>>): SearchSuggestionsResults {
        return fakeSuggestionIndex.getSuggestions(suggestionRequest, this::nameMatching)
    }

    private fun transformMetadata(item: ChannelMetadata): Pair<String, ChannelMetadata> {
        return Pair(item.id, item.copy())
    }

    private fun nameMatching(
        index: Map<String, ChannelMetadata>,
        query: SuggestionQuery<ChannelMetadata>
    ): List<Suggestion> {
        val phrase = query.phrase
        val filtered = index
            .filter { entry -> filterIncludedChannels(query, entry) }
            .filter { entry -> filterEligibleForStream(query, entry) }
            .filter { entry -> filterExcludedTypes(query, entry) }
            .filter { entry -> filterIncludedTypes(query, entry) }
            .filter { entry -> entry.value.name.contains(phrase, ignoreCase = true) }
            .values.toList()

        return filtered.map {
            Suggestion(
                name = it.name,
                id = it.id
            )
        }
    }

    private fun filterIncludedChannels(
        query: SuggestionQuery<ChannelMetadata>,
        entry: Map.Entry<String, ChannelMetadata>
    ): Boolean {
        val isEmpty = query.accessRuleQuery!!.includedChannelIds.isNullOrEmpty()
        val isInIncludedChannels = query.accessRuleQuery.includedChannelIds.contains(
            entry.value.id
        )
        return isEmpty || isInIncludedChannels
    }

    private fun filterEligibleForStream(
        query: SuggestionQuery<ChannelMetadata>,
        entry: Map.Entry<String, ChannelMetadata>
    ): Boolean = query.accessRuleQuery?.isEligibleForStream?.let { entry.value.eligibleForStream == it } ?: true

    private fun filterExcludedTypes(
        query: SuggestionQuery<ChannelMetadata>,
        entry: Map.Entry<String, ChannelMetadata>
    ): Boolean {
        val isEmpty = query.accessRuleQuery!!.excludedTypes.isNullOrEmpty()
        val hasNoExcludedTypes = entry.value.contentTypes.none { query.accessRuleQuery.excludedTypes.contains(it) }

        return isEmpty || hasNoExcludedTypes
    }

    private fun filterIncludedTypes(
        query: SuggestionQuery<ChannelMetadata>,
        entry: Map.Entry<String, ChannelMetadata>
    ): Boolean {
        val isEmpty = query.accessRuleQuery!!.includedTypes.isNullOrEmpty()
        val hasAnyIncludedType = entry.value.contentTypes.any { query.accessRuleQuery.includedTypes.contains(it) }

        return isEmpty || hasAnyIncludedType
    }

    private fun performSearch(index: Map<String, ChannelMetadata>, query: ChannelQuery): List<String> {
        return index.filter { item ->
            query.ingestTypes.isEmpty() || query.ingestTypes.contains(item.value.ingestType)
        }.map { it.key }
    }
}
