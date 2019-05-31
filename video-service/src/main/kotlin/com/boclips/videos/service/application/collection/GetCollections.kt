package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.UnauthorizedException
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.CollectionSearchQuery
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.SubjectId
import com.boclips.videos.service.domain.model.collection.UserId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import currentUserHasRole
import getCurrentUserId

class GetCollections(
    private val collectionService: CollectionService,
    private val collectionRepository: CollectionRepository,
    private val collectionResourceFactory: CollectionResourceFactory
) {
    operator fun invoke(collectionFilter: CollectionFilter): Page<CollectionResource> {
        val pageRequest =
            PageRequest(collectionFilter.pageNumber, collectionFilter.pageSize)

        if (requiresTextSearching(collectionFilter)) {
            return collectionService.search(
                CollectionSearchQuery(
                    collectionFilter.query!!,
                    collectionFilter.pageSize,
                    collectionFilter.pageNumber
                )
            ).let {
                assemblePage(
                    collectionFilter, PageInfo(false), it
                )
            }
        }

        return fetchByVisibility(collectionFilter, pageRequest)
            .let { collection -> assemblePage(collectionFilter, collection.pageInfo, collection.elements) }
    }

    private fun requiresTextSearching(collectionFilter: CollectionFilter) =
        collectionFilter.query != null

    private fun assemblePage(
        collectionFilter: CollectionFilter,
        pageInfo: PageInfo,
        collections: Iterable<Collection>
    ): Page<CollectionResource> {
        return Page(
            collections.map {
                collectionResourceFactory.buildCollectionResource(
                    it,
                    collectionFilter.projection
                )
            },
            pageInfo
        )
    }

    private fun fetchByVisibility(
        collectionFilter: CollectionFilter,
        pageRequest: PageRequest
    ): Page<Collection> {
        return when (collectionFilter.visibility) {
            CollectionFilter.Visibility.PUBLIC -> collectionRepository.getPublic(
                pageRequest,
                collectionFilter.subjects.map {
                    SubjectId(it)
                })
            CollectionFilter.Visibility.BOOKMARKED -> collectionRepository.getBookmarked(
                pageRequest,
                getCurrentUserId()
            )
            CollectionFilter.Visibility.PRIVATE -> {
                val owner = validatePrivateCollectionsOwnerOrThrow(collectionFilter)
                collectionRepository.getByOwner(owner, pageRequest)
            }
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