package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter

class CollectionSearchServiceFake : AbstractInMemoryFake<CollectionQuery, CollectionMetadata>(),
    IndexReader<CollectionMetadata, CollectionQuery>,
    IndexWriter<CollectionMetadata> {

    override fun upsertMetadata(index: MutableMap<String, CollectionMetadata>, item: CollectionMetadata) {
        index[item.id] = item.copy()
    }

    override fun idsMatching(
        index: MutableMap<String, CollectionMetadata>,
        query: CollectionQuery
    ): List<String> {
        val phrase = query.phrase

        return index
            .filter { entry -> if (phraseQuery(query)) entry.value.title.contains(phrase, ignoreCase = true) else true }
            .filter { entry -> if (subjectQuery(query)) entry.value.subjectIds.any { query.subjectIds.contains(it) } else true }
            .filter { entry -> if (visibilityQuery(query)) entry.value.visibility == query.visibility else true }
            .map { collection -> collection.key }
    }

    private fun subjectQuery(collectionQuery: CollectionQuery) =
        collectionQuery.subjectIds.isNotEmpty()

    private fun phraseQuery(collectionQuery: CollectionQuery) =
        collectionQuery.phrase.isNotEmpty()

    private fun visibilityQuery(collectionQuery: CollectionQuery) =
        collectionQuery.visibility != null
}