package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.videos.model.AgeRange

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
            .filter { entry ->
                if (phraseQuery(query)) entry.value.title.contains(phrase, ignoreCase = true) else true
            }
            .filter { entry ->
                if (subjectQuery(query)) entry.value.subjectIds.any { query.subjectIds.contains(it) } else true
            }
            .filter { entry ->
                val visibilityMatches = if (query.visibilityForOwners.isNotEmpty()) query.visibilityForOwners.any {
                    it.visibility.contains(entry.value.visibility)
                        && it.owner?.let { owner -> owner == entry.value.owner } ?: true
                } else true

                val bookmarkMatches =
                    if (bookmarkQuery(query)) entry.value.bookmarkedByUsers.contains(query.bookmarkedBy) else true

                if (query.visibilityForOwners.any { it.owner != null } && bookmarkQuery(query)) {
                    bookmarkMatches || visibilityMatches
                } else {
                    bookmarkMatches && visibilityMatches
                }
            }
            .filter { entry ->
                if (query.hasLessonPlans != null) query.hasLessonPlans == entry.value.hasLessonPlans else true
            }
            .filter { entry ->
                if (query.permittedIds != null)
                    query.permittedIds.contains(entry.value.id)
                else
                    true
            }
            .filter { entry ->
                if (query.ageRangeMin == null && query.ageRangeMax == null) {
                    return@filter true
                }

                return@filter (entry.value.ageRangeMin == query.ageRangeMin && entry.value.ageRangeMax == query.ageRangeMax)
            }
            .filter { entry ->
                if (query.ageRanges.isNullOrEmpty()) {
                    return@filter true
                }

                return@filter query.ageRanges.any { ageRange ->
                    AgeRange(entry.value.ageRangeMin, entry.value.ageRangeMax) == ageRange
                }
            }
            .map { collection -> collection.key }
    }

    private fun subjectQuery(collectionQuery: CollectionQuery) =
        collectionQuery.subjectIds.isNotEmpty()

    private fun phraseQuery(collectionQuery: CollectionQuery) =
        collectionQuery.phrase.isNotEmpty()

    private fun bookmarkQuery(collectionQuery: CollectionQuery) =
        collectionQuery.bookmarkedBy != null
}
