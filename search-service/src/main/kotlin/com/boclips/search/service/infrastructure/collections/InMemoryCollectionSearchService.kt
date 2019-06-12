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
        query: CollectionQuery
    ): List<String> {
        val phrase = query.phrase

        return index
            .filter { entry ->
                when {
                    phraseQuery(query) -> entry.value.title.contains(phrase!!, ignoreCase = true)
                    subjectQuery(query) -> entry.value.subjectIds.any { query.subjectIds.contains(it) }
                    else -> true
                }
            }
            .map { collection -> collection.key }
    }

    private fun subjectQuery(collectionQuery: CollectionQuery) =
        collectionQuery.subjectIds.isNotEmpty()

    private fun phraseQuery(collectionQuery: CollectionQuery) =
        collectionQuery.phrase != null
}
