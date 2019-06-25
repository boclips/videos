package com.boclips.search.service.domain.collections

import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.model.PaginatedSearchRequest

abstract class CollectionSearchServiceAdapter<T>(
    private val readSearchService: ReadSearchService<CollectionMetadata, CollectionQuery>,
    private val writeSearchService: WriteSearchService<CollectionMetadata>
) : ReadSearchService<CollectionMetadata, CollectionQuery>, WriteSearchService<T> {

    override fun safeRebuildIndex(collections: Sequence<T>, notifier: ProgressNotifier?) {
        writeSearchService.safeRebuildIndex(collections.map(::convert), notifier)
    }

    override fun upsert(collections: Sequence<T>, notifier: ProgressNotifier?) {
        writeSearchService.upsert(collections.map(::convert), notifier)
    }

    override fun search(searchRequest: PaginatedSearchRequest<CollectionQuery>): List<String> {
        return readSearchService.search(searchRequest)
    }

    override fun count(collectionQuery: CollectionQuery): Long {
        return readSearchService.count(collectionQuery)
    }

    override fun removeFromSearch(collectionId: String) {
        writeSearchService.removeFromSearch(collectionId)
    }

    override fun bulkRemoveFromSearch(items: List<String>) {
        writeSearchService.bulkRemoveFromSearch(items)
    }

    abstract fun convert(document: T): CollectionMetadata
}
