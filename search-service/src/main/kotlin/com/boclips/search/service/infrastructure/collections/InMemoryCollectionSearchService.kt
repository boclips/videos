package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.infrastructure.AbstractInMemorySearchService

class InMemoryCollectionSearchService : AbstractInMemorySearchService<CollectionQuery, CollectionMetadata>(),
    ReadSearchService<CollectionMetadata, CollectionQuery>, WriteSearchService<CollectionMetadata> {
    override fun upsertMetadata(index: MutableMap<String, CollectionMetadata>, item: CollectionMetadata) {
        index[item.id] = item.copy()
    }

    override fun idsMatching(
        index: MutableMap<String, CollectionMetadata>,
        collectionQuery: CollectionQuery
    ): List<String> {
        val phrase = collectionQuery.phrase

        return index
            .filter { entry ->
                entry.value.title.contains(phrase!!, ignoreCase = true)
            }
            .map { collection -> collection.key }
    }
}
