package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.FacetCount
import com.boclips.search.service.domain.common.FacetType
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.ResultCounts
import com.boclips.search.service.domain.common.SearchResults
import com.boclips.search.service.domain.common.model.CursorBasedIndexSearchRequest
import com.boclips.search.service.domain.common.model.FacetDefinition
import com.boclips.search.service.domain.common.model.IndexSearchRequest
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.common.model.PagingCursor
import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import java.util.UUID

class FakeSearchIndex<QUERY : SearchQuery<METADATA>, METADATA> {
    private val index = mutableMapOf<String, METADATA>()
    private var facetCounts: List<FacetCount> = emptyList()
    private var requests: MutableList<IndexSearchRequest<QUERY>> = mutableListOf()
    private var cursorPosition: Int = 0
    private var currentCursor: PagingCursor? = null

    fun search(
        searchRequest: IndexSearchRequest<QUERY>,
        filter: (index: MutableMap<String, METADATA>, query: QUERY) -> List<String>,
        defaultSort: ((a: METADATA, b: METADATA) -> Int)? = null,
    ): SearchResults {
        val startIndex = (searchRequest as? PaginatedIndexSearchRequest)?.startIndex
        (searchRequest as? CursorBasedIndexSearchRequest)?.apply {
            if (cursor != currentCursor || cursor == null) {
                currentCursor = cursor ?: UUID.randomUUID()
                    .toString()
                    .let(::PagingCursor)
                cursorPosition = 0
            }
        }
        val toDrop: Int = startIndex ?: cursorPosition

        if ((searchRequest.query.facetDefinition as? FacetDefinition.Video)?.includePriceFacets != true) {
            facetCounts = facetCounts.filterNot { it.type == FacetType.Prices }
        }

        requests.add(searchRequest)

        val matchingIds = filter(index, searchRequest.query)

        val elements = sort(matchingIds, searchRequest.query, defaultSort)
            .drop(toDrop)
            .take(searchRequest.windowSize)

        if (searchRequest.isCursorBased()) {
            cursorPosition += searchRequest.windowSize
        }

        return SearchResults(
            elements = elements,
            counts = ResultCounts(
                totalHits = matchingIds.size.toLong(),
                facets = facetCounts
            ),
            cursor = currentCursor
        )
    }

    fun upsert(
        items: Sequence<METADATA>,
        transformMetadata: (item: METADATA) -> Pair<String, METADATA>,
        notifier: ProgressNotifier?
    ) {
        items.forEach { item ->
            val pair = transformMetadata(item)
            index[pair.first] = pair.second
        }

        notifier?.complete()
    }

    fun safeRebuildIndex(
        items: Sequence<METADATA>,
        transformMetadata: (item: METADATA) -> Pair<String, METADATA>,
        notifier: ProgressNotifier?
    ) {
        clear()
        upsert(items, transformMetadata, notifier)
    }

    fun removeFromSearch(itemId: String) {
        index.remove(itemId)
    }

    fun bulkRemoveFromSearch(itemIds: List<String>) {
        itemIds.forEach(this::removeFromSearch)
    }

    fun clear() {
        cursorPosition = 0
        currentCursor = null
        index.clear()
    }

    fun setFacets(facets: List<FacetCount>) {
        this.facetCounts = facets
    }

    fun getLastSearchRequest(): IndexSearchRequest<QUERY> {
        return this.requests.last()
    }

    private fun sort(ids: List<String>, query: QUERY, defaultSort: ((a: METADATA, b: METADATA) -> Int)?): List<String> {
        if (query.sort.isEmpty()) return ids.sortedWith(
            Comparator(applyDefaultSort(defaultSort))
        )

        return when (val sortCritieria = query.sort.first()) {
            is Sort.ByField -> {
                val sortedIds = ids.sortedWith(compareBy<String> {
                    val value: Comparable<*>? = sortCritieria.fieldName.get(index[it]!!)
                    /**
                     * Kotlin isn't happy about the * to Any cast.. This is the safest way we can coerce the type without
                     * littering the entire code base with the Sort generic type.
                     *
                     * We cannot define sort.fieldName as a Comparable<Any> as it won't then allow us to reference Comparables
                     */
                    @Suppress("UNCHECKED_CAST")
                    value as? Comparable<Any>
                }.thenComparator(applyDefaultSort(defaultSort)))

                when (sortCritieria.order) {
                    SortOrder.ASC -> sortedIds
                    SortOrder.DESC -> sortedIds.reversed()
                }
            }
            is Sort.ByRandom -> ids.shuffled()
        }
    }

    private fun applyDefaultSort(defaultSort: ((aValue: METADATA, bValue: METADATA) -> Int)?) =
        { a: String, b: String ->
            val aValue = index[a]!!
            val bValue = index[b]!!
            defaultSort?.let { it(aValue, bValue) } ?: 0
        }
}
