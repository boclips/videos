package com.boclips.videos.service.presentation.event

import com.boclips.eventbus.events.collection.CollectionInteractionType
import com.boclips.videos.service.application.analytics.InvalidEventException

data class CollectionInteractedWithEventCommand(val subtype: String) : EventCommand() {
    override fun isValidOrThrows() {
        if (this.subtype.isNullOrBlank())
            throw InvalidEventException("subtype must be specified")

        try {
            CollectionInteractionType.valueOf(subtype)
        } catch (e: Exception) {
            throw InvalidEventException("subtype is not recognised for: $subtype")
        }
    }

    fun getSubtype(): CollectionInteractionType {
        isValidOrThrows()
        return CollectionInteractionType.valueOf(subtype)
    }
}
