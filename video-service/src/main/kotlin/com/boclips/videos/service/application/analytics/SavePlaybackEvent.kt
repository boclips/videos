package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import mu.KLogging
import java.time.ZoneOffset
import java.time.ZonedDateTime

class SavePlaybackEvent(private val eventService: EventService) {
    companion object : KLogging()

    fun execute(event: CreatePlaybackEventCommand?, user: User) {
        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        eventService.savePlaybackEvent(
            videoId = VideoId(event.videoId!!),
            videoIndex = event.videoIndex,
            segmentStartSeconds = event.segmentStartSeconds!!,
            segmentEndSeconds = event.segmentEndSeconds!!,
            query = event.query,
            timestamp = event.captureTime ?: ZonedDateTime.now(),
            user = user
        )
    }

    fun execute(events: List<CreatePlaybackEventCommand>?, user: User) {
        events ?: throw InvalidEventException("Event cannot be null")

        validateCreatePlaybackEvents(events)

        logger.info { "Received batch of ${events.size} playback events by user ${user.id}" }

        events.forEach { event ->
            eventService.savePlaybackEvent(
                videoId = VideoId(event.videoId!!),
                videoIndex = event.videoIndex,
                segmentStartSeconds = event.segmentStartSeconds!!,
                segmentEndSeconds = event.segmentEndSeconds!!,
                timestamp = event.captureTime?.toInstant()?.atZone(ZoneOffset.UTC) ?: ZonedDateTime.now(),
                query = event.query,
                user = user
            )
        }
    }
}

