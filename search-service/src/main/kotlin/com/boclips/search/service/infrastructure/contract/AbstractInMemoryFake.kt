package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.ResultCounts
import com.boclips.search.service.domain.common.FacetCount
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.SearchResults
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder

abstract class AbstractInMemoryFake<QUERY : SearchQuery<METADATA>, METADATA> :
    IndexReader<METADATA, QUERY>,
    IndexWriter<METADATA> {
    private val index = mutableMapOf<String, METADATA>()
    private var facetCounts: List<FacetCount> = emptyList()
    private var requests: MutableList<PaginatedSearchRequest<QUERY>> = mutableListOf()

    override fun search(searchRequest: PaginatedSearchRequest<QUERY>): SearchResults {
        requests.add(searchRequest)

        val idsMatching = idsMatching(index, searchRequest.query)

        val elements = sort(idsMatching, searchRequest.query)
            .drop(searchRequest.startIndex)
            .take(searchRequest.windowSize)

        return SearchResults(
            elements = elements,
            counts = ResultCounts(
                totalHits = idsMatching.size.toLong(),
                facets = facetCounts
            )
        )
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

    fun setFacets(facetCounts: List<FacetCount>) {
        this.facetCounts = facetCounts
    }

    fun getLastSearchRequest(): PaginatedSearchRequest<QUERY> {
        return requests.last()
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

    abstract fun idsMatching(index: MutableMap<String, METADATA>, query: QUERY): List<String>
    abstract fun upsertMetadata(index: MutableMap<String, METADATA>, item: METADATA)
}
