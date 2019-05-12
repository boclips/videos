package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.UnauthorizedException
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
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
                throwIfUnauthorized(collectionFilter)
                collectionRepository.getByOwner(getCurrentUserId(), pageRequest)
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

    private fun throwIfUnauthorized(collectionFilter: CollectionFilter) {
        val authenticatedUserId = getCurrentUserId().value
        if (collectionFilter.owner == null || collectionFilter.owner != authenticatedUserId) {
            throw UnauthorizedException("$authenticatedUserId is not authorized to access ${collectionFilter.owner}")
        }
    }
}