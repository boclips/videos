package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.FacetCount
import com.boclips.search.service.domain.common.FacetType
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
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

abstract class AbstractInMemoryFake<QUERY : SearchQuery<METADATA>, METADATA> :
    IndexReader<METADATA, QUERY>,
    IndexWriter<METADATA> {
    private val index = mutableMapOf<String, METADATA>()
    private var facetCounts: List<FacetCount> = emptyList()
    private var requests: MutableList<IndexSearchRequest<QUERY>> = mutableListOf()
    private var cursorPosition: Int = 0
    private var currentCursor: PagingCursor? = null

    fun getIndex() = index.toMap()

    override fun search(searchRequest: IndexSearchRequest<QUERY>): SearchResults {
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

        val idsMatching = idsMatching(index, searchRequest.query)

        val elements = sort(idsMatching, searchRequest.query)
            .drop(toDrop)
            .take(searchRequest.windowSize)

        if (searchRequest.isCursorBased()) {
            cursorPosition += searchRequest.windowSize
        }


        return SearchResults(
            elements = elements,
            counts = ResultCounts(
                totalHits = idsMatching.size.toLong(),
                facets = facetCounts
            ),
            cursor = currentCursor
        )
    }

    override fun upsert(items: Sequence<METADATA>, notifier: ProgressNotifier?) {
        items.forEach { video ->
            upsertMetadata(index, video)
        }
    }

    override fun safeRebuildIndex(items: Sequence<METADATA>, notifier: ProgressNotifier?) {
        clear()
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
        cursorPosition = 0
        currentCursor = null
        index.clear()
    }

    fun setFacets(facetCounts: List<FacetCount>) {
        this.facetCounts = facetCounts
    }

    fun getLastSearchRequest(): IndexSearchRequest<QUERY> {
        return requests.last()
    }

    private fun sort(ids: List<String>, query: QUERY): List<String> {
        if (query.sort.isEmpty()) return ids

        return when (val sortCritieria = query.sort.first()) {
            is Sort.ByField -> {
                val sortedIds = ids.sortedBy {
                    val value: Comparable<*>? = sortCritieria.fieldName.get(index[it]!!)
                    /**
                     * Kotlin isn't happy about the * to Any cast.. This is the safest way we can coerce the type without
                     * littering the entire code base with the Sort generic type.
                     *
                     * We cannot define sort.fieldName as a Comparable<Any> as it won't then allow us to reference Comparables
                     */
                    @Suppress("UNCHECKED_CAST")
                    value as? Comparable<Any>
                }

                when (sortCritieria.order) {
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
