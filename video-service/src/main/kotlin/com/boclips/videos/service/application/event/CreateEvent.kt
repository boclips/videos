package com.boclips.videos.service.application.event

import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.infrastructure.email.NoResultsEmail
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.types.NoSearchResultsEvent
import com.boclips.videos.service.infrastructure.event.types.PlaybackEvent
import com.boclips.videos.service.presentation.event.CreateNoSearchResultsEventCommand
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import mu.KLogging
import java.time.ZonedDateTime

class CreateEvent(
        private val eventService: EventService,
        private val emailClient: EmailClient
) {
    companion object : KLogging()

    fun createPlaybackEvent(event: CreatePlaybackEventCommand?) {
        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        eventService.saveEvent(PlaybackEvent(
                playerId = event.playerId!!,
                videoId = event.videoId!!,
                segmentStartSeconds = event.segmentStartSeconds!!,
                segmentEndSeconds = event.segmentEndSeconds!!,
                videoDurationSeconds = event.videoDurationSeconds!!,
                captureTime = extractZonedDateTime(event),
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

        emailClient.send(NoResultsEmail(
                name = event.name,
                email = event.email,
                query = event.query,
                description = event.description
        ))
    }

    private fun extractZonedDateTime(event: CreatePlaybackEventCommand): ZonedDateTime {
        return try {
            ZonedDateTime.parse(event.captureTime)
        } catch (ex: Exception) {
            logger.warn { "Failed parsing capture time provided by client using current timestamp instead." }
            ZonedDateTime.now()
        }
    }
}
