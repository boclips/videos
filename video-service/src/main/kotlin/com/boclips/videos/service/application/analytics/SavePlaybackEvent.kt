package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand

class SavePlaybackEvent(
    private val eventService: EventService
) {
    fun execute(event: CreatePlaybackEventCommand?, playbackDevice: String?) {

        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        eventService.savePlaybackEvent(
            playerId = event.playerId!!,
            videoId = VideoId(event.videoId!!),
            videoIndex = event.videoIndex,
            segmentStartSeconds = event.segmentStartSeconds!!,
            segmentEndSeconds = event.segmentEndSeconds!!,
            videoDurationSeconds = event.videoDurationSeconds!!,
            playbackDevice = playbackDevice
        )
    }
}
