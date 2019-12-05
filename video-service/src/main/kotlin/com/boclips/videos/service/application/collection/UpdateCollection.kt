package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.application.getCurrentUser
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest

class UpdateCollection(
    private val collectionSearchService: CollectionSearchService,
    private val collectionRepository: CollectionRepository,
    private val collectionUpdatesConverter: CollectionUpdatesConverter,
    private val collectionAccessService: CollectionAccessService
) {
    operator fun invoke(collectionId: String, updateCollectionRequest: UpdateCollectionRequest?) {
        val collection = collectionRepository.find(CollectionId(value = collectionId))
            ?: throw CollectionNotFoundException(collectionId)

        if (!collectionAccessService.hasWriteAccess(collection, getCurrentUser())) {
            throw CollectionAccessNotAuthorizedException(getCurrentUserId(), collectionId)
        }

        val commands = collectionUpdatesConverter.convert(collection.id, updateCollectionRequest)

        collectionRepository.update(*commands)

        collectionRepository.find(collection.id)?.let { updatedCollection ->
            collectionSearchService.upsert(sequenceOf(updatedCollection))
        }
    }
}
