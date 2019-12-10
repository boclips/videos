package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand

fun validateCreatePlaybackEvents(playbackEvents: List<CreatePlaybackEventCommand>?) {
    if (playbackEvents.isNullOrEmpty()) {
        throw InvalidEventException("Event cannot be null or empty")
    }

    if (playbackEvents.size == 1) {
        playbackEvents[0].isValidOrThrows()
        return
    }

    playbackEvents.forEach { event: CreatePlaybackEventCommand ->
        event.isValidOrThrows()
        if (event.captureTime == null) throw InvalidEventException(
            "Event captureTime cannot be null"
        )
    }

    return
}
