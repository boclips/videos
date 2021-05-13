package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.suggestions.IndexReader
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.infrastructure.common.suggestions.AbstractInMemoryFakeSuggestions
import java.util.*
import kotlin.Comparator

class ChannelIndexFake :
    AbstractInMemoryFakeSuggestions<SuggestionQuery<ChannelMetadata>, ChannelMetadata>(),
    IndexReader<ChannelMetadata, SuggestionQuery<ChannelMetadata>>,
    IndexWriter<ChannelMetadata> {
    override fun upsertMetadata(index: MutableMap<String, ChannelMetadata>, item: ChannelMetadata) {
        index[item.id] = item.copy()
    }

    override fun nameMatching(
        index: MutableMap<String, ChannelMetadata>,
        query: SuggestionQuery<ChannelMetadata>
    ): List<Suggestion> {
        val phrase = query.phrase
        val sort = query.sort
        val filtered = index
            .filter { entry -> filterIncludedChannels(query, entry) }
            .filter { entry -> filterEligibleForStream(query, entry) }
            .filter { entry -> filterExcludedTypes(query, entry) }
            .filter { entry -> filterIncludedTypes(query, entry) }
            .filter { entry -> entry.value.name.contains(phrase, ignoreCase = true) }
            .values.toList()

        val sorted = sort.find { (it as? Sort.ByField)?.fieldName == ChannelMetadata::taxonomy }?.let {
            filtered.sortedWith((Comparator { a, b -> getTaxonomySortPriority(a).compareTo(getTaxonomySortPriority(b)) }))
        } ?: filtered

        return sorted.map {
            Suggestion(
                name = it.name,
                id = it.id
            )
        }
    }

    private fun getTaxonomySortPriority(channelMetadata: ChannelMetadata): String {
        val taxonomy = channelMetadata.taxonomy
        return if (taxonomy.videoLevelTagging) {
            "1"
        } else if (taxonomy.categories == null || taxonomy.categories.isEmpty()) {
            "0"
        } else {
            taxonomy.categories.map { it.value }.sorted().first()
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
}
