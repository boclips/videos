package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor.currentUserHasRole
import com.boclips.users.client.UserServiceClient
import com.boclips.videos.service.application.UnauthorizedException
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetCollections(
    private val collectionService: CollectionService,
    private val collectionRepository: CollectionRepository,
    private val collectionResourceFactory: CollectionResourceFactory,
    private val getContractedCollections: GetContractedCollections,
    private val userServiceClient: UserServiceClient
) {
    operator fun invoke(collectionFilter: CollectionFilter): Page<CollectionResource> {
        return getCollections(collectionFilter).let {
            assembleResourcesPage(
                collectionFilter,
                it.pageInfo,
                it.elements
            )
        }
    }

    private fun getCollections(collectionFilter: CollectionFilter): Page<Collection> {
        val userContracts = userServiceClient.getContracts(getCurrentUserId().value)
        return when {
            userContracts.isNotEmpty() -> getContractedCollections(collectionFilter, userContracts)
            isPublicCollectionSearch(collectionFilter) -> collectionService.search(
                CollectionSearchQuery(
                    collectionFilter.query,
                    collectionFilter.subjects,
                    collectionFilter.pageSize,
                    collectionFilter.pageNumber
                )
            )
            else -> fetchByVisibility(collectionFilter)
        }
    }

    private fun isPublicCollectionSearch(collectionFilter: CollectionFilter) =
        collectionFilter.visibility == CollectionFilter.Visibility.PUBLIC

    private fun assembleResourcesPage(
        collectionFilter: CollectionFilter,
        pageInfo: PageInfo,
        collections: Iterable<Collection>
    ): Page<CollectionResource> {
        return Page(collections.map {
            collectionResourceFactory.buildCollectionResource(it, collectionFilter.projection)
        }, pageInfo)
    }

    private fun fetchByVisibility(
        collectionFilter: CollectionFilter
    ): Page<Collection> {
        val pageRequest = PageRequest(page = collectionFilter.pageNumber, size = collectionFilter.pageSize)
        return when (collectionFilter.visibility) {
            CollectionFilter.Visibility.BOOKMARKED -> collectionRepository.getBookmarkedByUser(
                pageRequest,
                getCurrentUserId()
            )
            CollectionFilter.Visibility.PRIVATE -> {
                val owner = validatePrivateCollectionsOwnerOrThrow(collectionFilter)
                collectionRepository.getByOwner(owner, pageRequest)
            }
            else -> throw IllegalStateException()
        }
    }

    private fun validatePrivateCollectionsOwnerOrThrow(collectionFilter: CollectionFilter): UserId {
        val owner = collectionFilter.owner
            ?: throw UnauthorizedException("owner must be specified for private collections access")
        val authenticatedUserId = getCurrentUserId().value

        if (owner == authenticatedUserId || currentUserHasRole(UserRoles.VIEW_ANY_COLLECTION)) {
            return UserId(owner)
        }

        throw UnauthorizedException("$authenticatedUserId is not authorized to access $owner")
    }
}
