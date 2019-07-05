package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.domain.service.collection.getReadableCollectionOrThrow
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.events.EventService

class UnbookmarkCollection(
    private val collectionRepository: CollectionRepository,
    private val eventService: EventService
) {
    operator fun invoke(collectionId: String) {
        val collection = collectionRepository.getReadableCollectionOrThrow(collectionId)
        if (collection.isMine()) throw CollectionIllegalOperationException(
            getCurrentUserId(),
            collectionId,
            "unbookmark your own collection"
        )

        collectionRepository.unbookmark(collection.id, getCurrentUserId())
        eventService.saveUnbookmarkCollectionEvent(collection.id)
    }
}
