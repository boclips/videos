package com.boclips.videos.service.application.analytics

import com.boclips.security.utils.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand

class SavePlaybackEvent(
    private val eventService: EventService
) {
    fun execute(
        event: CreatePlaybackEventCommand?,
        playbackDevice: String?,
        user: User
    ) {
        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        eventService.savePlaybackEvent(
            videoId = VideoId(event.videoId!!),
            videoIndex = event.videoIndex,
            segmentStartSeconds = event.segmentStartSeconds!!,
            segmentEndSeconds = event.segmentEndSeconds!!,
            playbackDevice = playbackDevice,
            user = user
        )
    }
}
