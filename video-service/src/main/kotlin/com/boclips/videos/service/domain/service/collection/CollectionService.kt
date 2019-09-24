package com.boclips.videos.service.domain.service.collection

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.security.utils.UserExtractor.currentUserHasRole
import com.boclips.videos.service.domain.service.IsContractedToView
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.service.AccessRuleService
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging

class CollectionService(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService,
    private val accessRuleService: AccessRuleService,
    private val isContractedToView: IsContractedToView
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

        val count = count(query)
        return Page(collections, PageInfo(hasMoreElements = count > query.pageIndexUpperBound(), totalElements = count))
    }

    fun count(collectionSearchQuery: CollectionSearchQuery): Long {
        logger.info { "Counted collections for query $collectionSearchQuery" }
        return collectionSearchService.count(collectionSearchQuery.toSearchQuery())
    }

    fun getOwnedCollectionOrThrow(collectionId: String) =
        getCollectionOrThrow(
            collectionId = collectionId,
            collectionRepository = collectionRepository,
            isForReading = false
        )

    fun getReadableCollectionOrThrow(collectionId: String) =
        getCollectionOrThrow(
            collectionId = collectionId,
            collectionRepository = collectionRepository,
            isForReading = true
        )

    private fun getCollectionOrThrow(
        collectionId: String,
        collectionRepository: CollectionRepository,
        isForReading: Boolean
    ): Collection {
        val userId = getCurrentUserId()
        val collection = collectionRepository.find(
            CollectionId(
                collectionId
            )
        )
            ?: throw CollectionNotFoundException(collectionId)

        return when {
            isForReading && collection.isPublic -> collection
            collection.owner == userId -> collection
            currentUserHasRole(UserRoles.VIEW_ANY_COLLECTION) -> collection
            isContractedToView(collection, accessRuleService.getRules(userId.value)) -> collection
            else -> throw CollectionAccessNotAuthorizedException(
                userId,
                collectionId
            )
        }
    }
}

