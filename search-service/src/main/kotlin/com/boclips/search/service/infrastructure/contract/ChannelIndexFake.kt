package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.suggestions.IndexReader
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.domain.common.IndexWriter

class ChannelIndexFake :
    AbstractInMemoryFakeChannel<ChannelQuery, ChannelMetadata>(),
    IndexReader<ChannelMetadata, ChannelQuery>,
    IndexWriter<ChannelMetadata> {
    override fun upsertMetadata(index: MutableMap<String, ChannelMetadata>, item: ChannelMetadata) {
        index[item.id] = item.copy()
    }

    override fun nameMatching(
        index: MutableMap<String, ChannelMetadata>,
        query: ChannelQuery
    ): List<Suggestion> {
        val phrase = query.phrase

        return index
            .filter { entry ->
                query.accessRuleQuery.includedChannelIds.isNullOrEmpty() || !query.accessRuleQuery.includedChannelIds.contains(
                    entry.value.id
                )
            }
            .filter { entry ->
                entry.value.name.contains(phrase, ignoreCase = true) ?: false
            }.map { it ->
                Suggestion(
                    name = it.value.name,
                    id = it.value.id
                )
            }
    }

    private fun phraseQuery(channelQuery: ChannelQuery) =
        channelQuery.phrase.isNotEmpty()
}
