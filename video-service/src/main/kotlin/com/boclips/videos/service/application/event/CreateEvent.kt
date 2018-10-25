package com.boclips.videos.service.application.event

import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.NoSearchResultsEvent
import com.boclips.videos.service.infrastructure.event.PlaybackEvent
import com.boclips.videos.service.presentation.event.CreateNoSearchResultsEventCommand
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import java.time.ZonedDateTime

class CreateEvent(private val eventService: EventService) {
    fun createPlaybackEvent(event: CreatePlaybackEventCommand?) {
        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        eventService.saveEvent(PlaybackEvent(
                playerId = event.playerId!!,
                videoId = event.videoId!!,
                segmentStartSeconds = event.segmentStartSeconds!!,
                segmentEndSeconds = event.segmentEndSeconds!!,
                videoDurationSeconds = event.videoDurationSeconds!!,
                captureTime = ZonedDateTime.now(),
                searchId = event.searchId
        ))
    }

    fun createNoSearchResultsEvent(event: CreateNoSearchResultsEventCommand?) {
        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        eventService.saveEvent(NoSearchResultsEvent(
                name = event.name,
                email = event.email!!,
                query = event.query!!,
                description = event.description,
                captureTime = ZonedDateTime.now()
        ))
    }
}
