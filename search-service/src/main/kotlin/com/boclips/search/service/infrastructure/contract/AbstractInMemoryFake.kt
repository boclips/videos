package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder

abstract class AbstractInMemoryFake<QUERY : SearchQuery<METADATA>, METADATA> :
    IndexReader<METADATA, QUERY>,
    IndexWriter<METADATA> {
    private val index = mutableMapOf<String, METADATA>()

    override fun count(query: QUERY): Long = idsMatching(index, query).size.toLong()

    override fun search(searchRequest: PaginatedSearchRequest<QUERY>): List<String> {
        val idsMatching = idsMatching(index, searchRequest.query)

        return sort(idsMatching, searchRequest.query)
            .drop(searchRequest.startIndex.toInt())
            .take(searchRequest.windowSize.toInt())
    }

    private fun sort(ids: List<String>, query: QUERY): List<String> {
        query.sort ?: return ids

        return when (query.sort) {
            is Sort.ByField -> {
                val sortedIds = ids.sortedBy {
                    val value: Comparable<*>? = query.sort.fieldName.get(index[it]!!)
                    /**
                     * Kotlin isn't happy about the * to Any cast.. This is the safest way we can coerce the type without
                     * littering the entire code base with the Sort generic type.
                     *
                     * We cannot define sort.fieldName as a Comparable<Any> as it won't then allow us to reference Comparables
                     */
                    @Suppress("UNCHECKED_CAST")
                    value as? Comparable<Any>
                }

                when (query.sort.order) {
                    SortOrder.ASC -> sortedIds
                    SortOrder.DESC -> sortedIds.reversed()
                }
            }
            is Sort.ByRandom -> ids.shuffled()
        }
    }

    override fun upsert(items: Sequence<METADATA>, notifier: ProgressNotifier?) {
        items.forEach { video ->
            upsertMetadata(index, video)
        }
    }

    override fun safeRebuildIndex(items: Sequence<METADATA>, notifier: ProgressNotifier?) {
        index.clear()
        upsert(items, notifier)
    }

    fun clear() {
        index.clear()
    }

    override fun removeFromSearch(itemId: String) {
        index.remove(itemId)
    }

    override fun bulkRemoveFromSearch(itemIds: List<String>) {
        itemIds.forEach(this::removeFromSearch)
    }

    override fun makeSureIndexIsThere() {
    }

    abstract fun idsMatching(index: MutableMap<String, METADATA>, query: QUERY): List<String>
    abstract fun upsertMetadata(index: MutableMap<String, METADATA>, item: METADATA)
}
