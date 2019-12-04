package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.presentation.event.CreatePlayerInteractedWithEvent

class SavePlayerInteractedWithEvent(val eventService: EventService) {
    fun execute(event: CreatePlayerInteractedWithEvent?) {

        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        eventService.savePlayerInteractedWithEvent(
            videoId = VideoId(event.videoId!!),
            currentTime = event.currentTime!!,
            subtype = event.subtype!!,
            payload = event.payload!!
        )
    }
}
