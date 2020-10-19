package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.suggestions.IndexReader
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.infrastructure.common.suggestions.AbstractInMemoryFakeSuggestions

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

        return index
            .filter { entry ->
                query.accessRuleQuery!!.includedChannelIds.isNullOrEmpty() || !query.accessRuleQuery.includedChannelIds.contains(
                    entry.value.id
                )
            }
            .filter { entry ->
                entry.value.name.contains(phrase, ignoreCase = true)
            }.map {
                Suggestion(
                    name = it.value.name,
                    id = it.value.id
                )
            }
    }
}
