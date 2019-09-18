package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.application.UnauthorizedException
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId

class GetUserPrivateCollections(private val collectionRepository: CollectionRepository) {
    operator fun invoke(collectionFilter: CollectionFilter): Page<Collection> {
        val pageRequest = PageRequest(page = collectionFilter.pageNumber, size = collectionFilter.pageSize)
        val owner = validatePrivateCollectionsOwnerOrThrow(collectionFilter)
        return collectionRepository.getByOwner(owner, pageRequest)
    }

    private fun validatePrivateCollectionsOwnerOrThrow(collectionFilter: CollectionFilter): UserId {
        val owner = collectionFilter.owner
            ?: throw UnauthorizedException("owner must be specified for private collections access")
        val authenticatedUserId = getCurrentUserId().value

        if (owner == authenticatedUserId || UserExtractor.currentUserHasRole(UserRoles.VIEW_ANY_COLLECTION)) {
            return UserId(owner)
        }

        throw UnauthorizedException("$authenticatedUserId is not authorized to access $owner")
    }
}