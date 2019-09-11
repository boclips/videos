package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.events.EventService

class BookmarkCollection(
    private val collectionRepository: CollectionRepository,
    private val eventService: EventService,
    private val collectionService: CollectionService
) {
    operator fun invoke(collectionId: String) {
        val collection = collectionService.getReadableCollectionOrThrow(collectionId)
        if (collection.isMine()) throw CollectionIllegalOperationException(
            getCurrentUserId(),
            collectionId,
            "bookmark your own collection"
        )

        collectionRepository.bookmark(collection.id, getCurrentUserId())
        eventService.saveBookmarkCollectionEvent(collection.id)
    }
}
