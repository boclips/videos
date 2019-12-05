package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.application.getCurrentUser
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.domain.service.collection.CollectionSearchService

class DeleteCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService,
    private val collectionAccessService: CollectionAccessService
) {
    operator fun invoke(collectionId: String) {
        val collection = collectionRepository.find(CollectionId(value = collectionId))
            ?: throw CollectionNotFoundException(collectionId)

        if (!collectionAccessService.hasWriteAccess(collection, getCurrentUser())) {
            throw CollectionAccessNotAuthorizedException(getCurrentUserId(), collectionId)
        }

        collectionRepository.delete(CollectionId(collectionId))

        collectionSearchService.removeFromSearch(collectionId)
    }
}
