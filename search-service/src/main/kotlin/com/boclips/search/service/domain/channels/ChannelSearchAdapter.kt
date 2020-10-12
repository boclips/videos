package com.boclips.search.service.domain.channels

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.model.SearchRequestWithoutPagination

abstract class ChannelSearchAdapter<T>(
    private val indexReader: IndexReader<ChannelMetadata, ChannelQuery>,
    private val indexWriter: IndexWriter<ChannelMetadata>
) : IndexReader<ChannelMetadata, ChannelQuery>, IndexWriter<T> {
    override fun safeRebuildIndex(items: Sequence<T>, notifier: ProgressNotifier?) {
        indexWriter.safeRebuildIndex(items.map(::convert), notifier)
    }

    override fun upsert(items: Sequence<T>, notifier: ProgressNotifier?) {
        indexWriter.upsert(items.map(::convert), notifier)
    }

    override fun search(searchRequest: SearchRequestWithoutPagination<ChannelQuery>): SearchChannelsResults {
        return indexReader.search(searchRequest)
    }

    override fun removeFromSearch(itemId: String) {
        indexWriter.removeFromSearch(itemId)
    }

    override fun bulkRemoveFromSearch(itemIds: List<String>) {
        indexWriter.bulkRemoveFromSearch(itemIds)
    }

    override fun makeSureIndexIsThere() {
        indexWriter.makeSureIndexIsThere()
    }

    abstract fun convert(document: T): ChannelMetadata
}
