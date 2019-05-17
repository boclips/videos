package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getOwnedCollectionOrThrow
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest

class UpdateCollection(
    private val collectionRepository: CollectionRepository,
    private val eventService: EventService
) {
    operator fun invoke(collectionId: String, updateCollectionRequest: UpdateCollectionRequest?) {
        collectionRepository.getOwnedCollectionOrThrow(collectionId)

        val commands = CollectionUpdatesConverter.convert(updateCollectionRequest)

        collectionRepository.update(CollectionId(collectionId), commands)
        eventService.saveUpdateCollectionEvent(CollectionId(collectionId), commands)
    }
}
