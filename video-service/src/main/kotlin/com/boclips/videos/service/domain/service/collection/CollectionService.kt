package com.boclips.videos.service.domain.service.collection

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.domain.model.CollectionSearchQuery
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging

class CollectionService(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService
) {
    companion object : KLogging()

    fun search(query: CollectionSearchQuery): Page<Collection> {
        val searchRequest = PaginatedSearchRequest(
            query = query.toSearchQuery(),
            startIndex = convertPageToIndex(query.pageSize, query.pageIndex),
            windowSize = query.pageSize
        )
        val collectionIds = collectionSearchService.search(searchRequest).map { CollectionId(value = it) }
        val collections = collectionRepository.findAll(collectionIds)

        logger.info { "Returning ${collections.size} collections for query $query" }

        return Page(collections, PageInfo(hasMoreElements = count(query) > query.pageIndexUpperBound()))
    }

    fun count(collectionSearchQuery: CollectionSearchQuery): Long {
        logger.info { "Counted collections for query $collectionSearchQuery" }
        return collectionSearchService.count(collectionSearchQuery.toSearchQuery())
    }

    fun updateSearchIndex(id: CollectionId) {
        collectionRepository.find(id)?.let {
            if (it.isPublic) {
                collectionSearchService.upsert(sequenceOf(it))
            } else {
                collectionSearchService.removeFromSearch(it.id.value)
            }
        }
    }
}

