package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.presentation.event.CreateCollectionInteractedWithEvent

class SaveCollectionInteractedWithEvent(
    private val eventService: EventService
) {
    fun execute(collectionId: String, event: CreateCollectionInteractedWithEvent?) {
        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        eventService.saveCollectionInteractedWithEvent(
            collectionId = collectionId,
            subtype = event.subtype!!
        )
    }
}
