package com.boclips.videos.service.application.collection

import com.boclips.security.utils.User
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.domain.service.collection.CollectionSearchService

class DeleteCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService,
    private val collectionAccessService: CollectionAccessService
) {
    operator fun invoke(collectionId: String, user: User) {
        val collection = collectionRepository.find(CollectionId(value = collectionId))
            ?: throw CollectionNotFoundException(collectionId)

        if (!collectionAccessService.hasWriteAccess(collection, user)) {
            throw CollectionAccessNotAuthorizedException(UserId(value = user.id), collectionId)
        }

        collectionRepository.delete(CollectionId(collectionId), user)

        collectionSearchService.removeFromSearch(collectionId)
    }
}
