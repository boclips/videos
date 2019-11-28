package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.presentation.event.CollectionInteractedWithEventCommand

class SaveCollectionInteractedWithEvent(
    private val eventService: EventService
) {
    fun execute(collectionId: String, eventCommand: CollectionInteractedWithEventCommand?) {
        eventCommand ?: throw InvalidEventException("Event cannot be null")
        eventCommand.isValidOrThrows()

        eventService.saveCollectionInteractedWithEvent(
            collectionId = collectionId,
            subtype = eventCommand.getSubtype()
        )
    }
}
