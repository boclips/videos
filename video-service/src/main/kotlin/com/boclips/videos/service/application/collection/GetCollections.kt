package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.UnauthorizedException
import com.boclips.videos.service.domain.model.Page
import com.boclips.videos.service.domain.model.PageRequest
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import getCurrentUserId

class GetCollections(
    private val collectionService: CollectionService,
    private val collectionResourceFactory: CollectionResourceFactory
) {
    operator fun invoke(collectionFilter: CollectionFilter): Page<CollectionResource> {
        val pageRequest = PageRequest(collectionFilter.pageNumber, collectionFilter.pageSize)

        return when (collectionFilter.visibility) {
            CollectionFilter.Visibility.PUBLIC -> collectionService.getPublic(pageRequest)
            CollectionFilter.Visibility.BOOKMARKED -> collectionService.getBookmarked(pageRequest, getCurrentUserId())
            CollectionFilter.Visibility.PRIVATE -> {
                throwIfUnauthorized(collectionFilter)
                collectionService.getByOwner(getCurrentUserId(), pageRequest)
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