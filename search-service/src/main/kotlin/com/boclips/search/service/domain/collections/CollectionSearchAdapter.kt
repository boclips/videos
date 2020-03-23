package com.boclips.search.service.domain.collections

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.Counts
import com.boclips.search.service.domain.common.SearchResults

abstract class CollectionSearchAdapter<T>(
    private val indexReader: IndexReader<CollectionMetadata, CollectionQuery>,
    private val indexWriter: IndexWriter<CollectionMetadata>
) : IndexReader<CollectionMetadata, CollectionQuery>,
    IndexWriter<T> {

    override fun safeRebuildIndex(items: Sequence<T>, notifier: ProgressNotifier?) {
        indexWriter.safeRebuildIndex(items.map(::convert), notifier)
    }

    override fun upsert(items: Sequence<T>, notifier: ProgressNotifier?) {
        indexWriter.upsert(items.map(::convert), notifier)
    }

    override fun search(searchRequest: PaginatedSearchRequest<CollectionQuery>): SearchResults {
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

    abstract fun convert(document: T): CollectionMetadata
}
