package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getOwnedCollectionOrThrow
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest

class UpdateCollection(
    private val collectionService: CollectionService,
    private val eventService: EventService
) {
    operator fun invoke(collectionId: String, updateCollectionRequest: UpdateCollectionRequest?) {
        collectionService.getOwnedCollectionOrThrow(collectionId)

        val commands = CollectionUpdatesConverter.convert(updateCollectionRequest)

        collectionService.update(CollectionId(collectionId), commands)
        eventService.saveUpdateCollectionEvent(CollectionId(collectionId), commands)
    }
}
