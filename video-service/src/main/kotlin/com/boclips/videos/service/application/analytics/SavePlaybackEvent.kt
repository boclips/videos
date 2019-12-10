package com.boclips.videos.service.application.analytics

import com.boclips.security.utils.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import java.time.ZoneOffset
import java.time.ZonedDateTime

class SavePlaybackEvent(private val eventService: EventService) {
    fun execute(event: CreatePlaybackEventCommand?, playbackDevice: String?, user: User) {
        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        eventService.savePlaybackEvent(
            videoId = VideoId(event.videoId!!),
            videoIndex = event.videoIndex,
            segmentStartSeconds = event.segmentStartSeconds!!,
            segmentEndSeconds = event.segmentEndSeconds!!,
            playbackDevice = playbackDevice,
            timestamp = event.captureTime ?: ZonedDateTime.now(),
            user = user
        )
    }

    fun execute(events: List<CreatePlaybackEventCommand>?, user: User) {
        events ?: throw InvalidEventException("Event cannot be null")

        validateCreatePlaybackEvents(events)

        events.forEach { event ->
            eventService.savePlaybackEvent(
                videoId = VideoId(event.videoId!!),
                videoIndex = event.videoIndex,
                segmentStartSeconds = event.segmentStartSeconds!!,
                segmentEndSeconds = event.segmentEndSeconds!!,
                playbackDevice = null,
                timestamp = event.captureTime?.toInstant()?.atZone(ZoneOffset.UTC) ?: ZonedDateTime.now(),
                user = user
            )
        }
    }
}

