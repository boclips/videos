package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.common.InvalidCursorException
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
            if (currentCursor == null && cursor != null) {
                throw InvalidCursorException(cursor)
            }

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
                ids.sortedWith(
                    Comparator { aId: String, bId: String ->
                        val aValue: Comparable<Any>? = (sortCritieria.fieldName.get(index[aId]!!) as Comparable<Any>?)
                        val bValue: Comparable<Any>? = (sortCritieria.fieldName.get(index[bId]!!) as Comparable<Any>?)

                        val sort = if (aValue == null && bValue == null) {
                            0
                        } else if (aValue == null) {
                            -1
                        } else if (bValue == null) {
                            1
                        } else {
                            aValue.compareTo(bValue)
                        }

                        if (sortCritieria.order == SortOrder.DESC) {
                            sort * -1
                        } else {
                            sort
                        }
                    }
                        .thenComparator(applyDefaultSort(defaultSort))
                )
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
