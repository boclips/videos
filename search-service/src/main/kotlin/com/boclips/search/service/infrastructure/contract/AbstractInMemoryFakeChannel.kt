package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.channels.IndexReader
import com.boclips.search.service.domain.channels.SearchChannelsResults
import com.boclips.search.service.domain.channels.model.ChannelSuggestion
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.SearchRequestWithoutPagination

abstract class AbstractInMemoryFakeChannel<QUERY : SearchQuery<METADATA>, METADATA> :
    IndexReader<METADATA, QUERY>,
    IndexWriter<METADATA> {
    private val index = mutableMapOf<String, METADATA>()
    private var requests: MutableList<SearchRequestWithoutPagination<QUERY>> = mutableListOf()

    override fun search(searchRequest: SearchRequestWithoutPagination<QUERY>): SearchChannelsResults {
        requests.add(searchRequest)

        val nameMatching = nameMatching(index, searchRequest.query)

        return SearchChannelsResults(
            elements = nameMatching.map { it ->
                ChannelSuggestion(
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

//    abstract fun idsMatching(index: MutableMap<String, METADATA>, query: QUERY): List<String>
    abstract fun nameMatching(index: MutableMap<String, METADATA>, query: QUERY): List<ChannelSuggestion>
    abstract fun upsertMetadata(index: MutableMap<String, METADATA>, item: METADATA)

}
