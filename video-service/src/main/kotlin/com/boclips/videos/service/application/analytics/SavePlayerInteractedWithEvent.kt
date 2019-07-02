package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.presentation.event.CreatePlayerInteractedWithEvent

class SavePlayerInteractedWithEvent(val eventService: EventService) {
    fun execute(event: CreatePlayerInteractedWithEvent?) {

        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        eventService.savePlayerInteractedWithEvent(
            playerId = event.playerId!!,
            videoId = VideoId(event.videoId!!),
            videoDurationSeconds = event.videoDurationSeconds!!,
            currentTime = event.currentTime!!,
            type = event.type!!,
            payload = event.payload!!
        )
    }
}
