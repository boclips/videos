package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.UnauthorizedException
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.UserId
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import currentUserHasRole
import getCurrentUserId

class GetCollections(
    private val collectionRepository: CollectionRepository,
    private val collectionResourceFactory: CollectionResourceFactory
) {
    operator fun invoke(collectionFilter: CollectionFilter): Page<CollectionResource> {
        val pageRequest =
            PageRequest(collectionFilter.pageNumber, collectionFilter.pageSize)

        return when (collectionFilter.visibility) {
            CollectionFilter.Visibility.PUBLIC -> collectionRepository.getPublic(pageRequest)
            CollectionFilter.Visibility.BOOKMARKED -> collectionRepository.getBookmarked(
                pageRequest,
                getCurrentUserId()
            )
            CollectionFilter.Visibility.PRIVATE -> {
                val owner = validatePrivateCollectionsOwnerOrThrow(collectionFilter)
                collectionRepository.getByOwner(owner, pageRequest)
            }
        }.let { collection ->
            Page(
                collection.elements.map {
                    collectionResourceFactory.buildCollectionResource(
                        it,
                        collectionFilter.projection
                    )
                },
                collection.pageInfo
            )
        }
    }

    private fun validatePrivateCollectionsOwnerOrThrow(collectionFilter: CollectionFilter): UserId {
        val owner = collectionFilter.owner ?: throw UnauthorizedException("owner must be specified for private collections access")
        val authenticatedUserId = getCurrentUserId().value

        if (owner == authenticatedUserId || currentUserHasRole(UserRoles.VIEW_ANY_COLLECTION)) {
            return UserId(owner)
        }

        throw UnauthorizedException("$authenticatedUserId is not authorized to access $owner")
    }
}