package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.domain.common.suggestions.SuggestionsIndexReader
import com.boclips.search.service.infrastructure.common.suggestions.AbstractInMemoryFakeSuggestions

class ChannelIndexFake :
    AbstractInMemoryFakeSuggestions<SuggestionQuery<ChannelMetadata>, ChannelQuery, ChannelMetadata>(),
    SuggestionsIndexReader<ChannelMetadata, SuggestionQuery<ChannelMetadata>>,
    IndexReader<ChannelMetadata, ChannelQuery>,
    IndexWriter<ChannelMetadata> {
    override fun upsertMetadata(index: MutableMap<String, ChannelMetadata>, item: ChannelMetadata) {
        index[item.id] = item.copy()
    }

    override fun nameMatching(
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

    override fun idsMatching(index: MutableMap<String, ChannelMetadata>, query: ChannelQuery): List<String> {
        return index.map { it.key }
    }
}
