package com.boclips.videos.service.domain.service.collection

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.videos.service.domain.model.CollectionSearchQuery
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class CollectionService(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService
) {
    companion object : KLogging()

    fun search(query: CollectionSearchQuery): List<com.boclips.videos.service.domain.model.collection.Collection> {
        val searchRequest = PaginatedSearchRequest(
            query = query.toSearchQuery(),
            startIndex = convertPageToIndex(query.pageSize, query.pageIndex),
            windowSize = query.pageSize
        )
        val collectionIds = collectionSearchService.search(searchRequest).map { CollectionId(value = it) }
        val collection = collectionRepository.findAll(collectionIds)

        logger.info { "Returning ${collection.size} collections for query $query" }

        return collection
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

