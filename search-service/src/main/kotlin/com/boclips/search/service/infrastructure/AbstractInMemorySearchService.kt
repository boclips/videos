package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.model.SearchQuery
import com.boclips.search.service.domain.model.SortOrder

abstract class AbstractInMemorySearchService<QUERY : SearchQuery<METADATA>, METADATA> : ReadSearchService<METADATA, QUERY>,
    WriteSearchService<METADATA> {
    private val index = mutableMapOf<String, METADATA>()

    override fun count(query: QUERY): Long = idsMatching(index, query).size.toLong()

    override fun search(searchRequest: PaginatedSearchRequest<QUERY>): List<String> {
        val idsMatching = idsMatching(index, searchRequest.query)

        return sort(idsMatching, searchRequest.query)
            .drop(searchRequest.startIndex)
            .take(searchRequest.windowSize)
    }

    private fun sort(ids: List<String>, query: QUERY): List<String> {
        query.sort ?: return ids

        val sortedIds = ids.sortedBy {
            val value: Comparable<*> = query.sort.fieldName.get(index[it]!!)
            /**
             * Kotlin isn't happy about the * to Any cast.. This is the safest way we can coerce the type without
             * littering the entire code base with the Sort generic type.
             *
             * We cannot define sort.fieldName as a Comparable<Any> as it won't then allow us to reference Comparables
             */
            @Suppress("UNCHECKED_CAST")
            value as Comparable<Any>
        }

        return when (query.sort.order) {
            SortOrder.ASC -> sortedIds
            SortOrder.DESC -> sortedIds.reversed()
        }
    }

    override fun upsert(videos: Sequence<METADATA>, notifier: ProgressNotifier?) {
        videos.forEach { video ->
            upsertMetadata(index, video)
        }
    }

    override fun safeRebuildIndex(videos: Sequence<METADATA>, notifier: ProgressNotifier?) {
        index.clear()
        upsert(videos, notifier)
    }

    override fun removeFromSearch(videoId: String) {
        index.remove(videoId)
    }

    abstract fun idsMatching(index: MutableMap<String, METADATA>, videoQuery: QUERY): List<String>
    abstract fun upsertMetadata(index: MutableMap<String, METADATA>, item: METADATA)
}
