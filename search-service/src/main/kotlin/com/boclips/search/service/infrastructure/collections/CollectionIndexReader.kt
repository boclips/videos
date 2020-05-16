package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.common.Do
import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.ResultCounts
import com.boclips.search.service.domain.common.SearchResults
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.infrastructure.collections.CollectionFilterCriteria.Companion.allCriteria
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder
import kotlin.reflect.full.createType

class CollectionIndexReader(val client: RestHighLevelClient) : IndexReader<CollectionMetadata, CollectionQuery> {
    companion object : KLogging();

    private val elasticSearchResultConverter = CollectionDocumentConverter()

    override fun search(searchRequest: PaginatedSearchRequest<CollectionQuery>): SearchResults {
        val results = search(searchRequest.query, searchRequest.startIndex, searchRequest.windowSize)

        val elements = results
            .map(elasticSearchResultConverter::convert)
            .map { it.id }

        return SearchResults(elements = elements, counts = ResultCounts(totalHits = results.totalHits?.value ?: 0L))
    }

    private fun search(collectionQuery: CollectionQuery, startIndex: Int, windowSize: Int): SearchHits {
        val query = SearchSourceBuilder().apply {
            query(CollectionEsQuery().mainQuery(collectionQuery))
            postFilter(allCriteria(collectionQuery))
            collectionQuery.sort?.let { applySort(it) }
        }

        val request = SearchRequest(
            arrayOf(CollectionsIndex.getIndexAlias()),
            query.from(startIndex).size(windowSize)
        )

        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun SearchSourceBuilder.applySort(sort: Sort<CollectionMetadata>) {
        Do exhaustive when (sort) {
            is Sort.ByField -> {
                val suffix = if (sort.fieldName.returnType == String::class.createType()) {
                    ".keyword"
                } else {
                    ""
                }

                this.sort(
                    sort.fieldName.name + suffix,
                    SortOrder.fromString(sort.order.toString())
                )
            }
            else -> null
        }
    }
}
