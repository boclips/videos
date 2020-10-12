package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.channels.IndexReader
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.channels.model.ChannelSuggestion
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
    ): List<ChannelSuggestion> {
        val phrase = query.phrase

        return index
            .filter { entry ->
                if (phraseQuery(query)) entry.value.name.contains(phrase, ignoreCase = true) else true
            }.map { it ->
                ChannelSuggestion(
                    name = it.value.name,
                    id = it.value.id
                )
            }
    }

    private fun phraseQuery(channelQuery: ChannelQuery) =
        channelQuery.phrase.isNotEmpty()
}
