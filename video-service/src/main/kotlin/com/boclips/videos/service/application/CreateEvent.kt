package com.boclips.videos.service.application

import com.boclips.videos.service.infrastructure.event.Event
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.presentation.CreatePlaybackEventCommand
import java.time.ZonedDateTime

data class PlaybackEventData(
        val playerIdentifier: String,
        val videoIdentifier: String,
        val segmentStartSeconds: Long,
        val segmentEndSeconds: Long,
        val videoDurationSeconds: Long,
        val searchId: String?
)

class PlaybackEvent(
        playerIdentifier: String,
        videoIdentifier: String,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
        videoDurationSeconds: Long,
        captureTime: ZonedDateTime,
        searchId: String?
) : Event<PlaybackEventData>("PLAYBACK", captureTime, PlaybackEventData(
        playerIdentifier = playerIdentifier,
        videoIdentifier = videoIdentifier,
        segmentStartSeconds = segmentStartSeconds,
        segmentEndSeconds = segmentEndSeconds,
        videoDurationSeconds = videoDurationSeconds,
        searchId = searchId
))

class CreateEvent(private val eventService: EventService) {
    fun execute(event: CreatePlaybackEventCommand?) {
        event ?: throw InvalidEventException("Event cannot be null")

        if (event.playerIdentifier.isNullOrBlank()) throw InvalidEventException("playerIdentifier must be specified")
        if (event.videoIdentifier.isNullOrBlank()) throw InvalidEventException("videoIdentifier must be specified")

        if (isNullOrNegative(event.segmentEndSeconds)) throw InvalidEventException("segmentEndSeconds must be specified")
        if (isNullOrNegative(event.segmentStartSeconds)) throw InvalidEventException("segmentStartSeconds must be specified")
        if (isNullOrNegative(event.videoDurationSeconds)) throw InvalidEventException("videoDurationSeconds must be specified")

        eventService.saveEvent(PlaybackEvent(
                playerIdentifier = event.playerIdentifier!!,
                videoIdentifier = event.videoIdentifier!!,
                segmentStartSeconds = event.segmentStartSeconds!!,
                segmentEndSeconds = event.segmentEndSeconds!!,
                videoDurationSeconds = event.videoDurationSeconds!!,
                captureTime = parseZonedDateTime(event.captureTime),
                searchId = event.searchId
        ))
    }

    private fun parseZonedDateTime(value: String?): ZonedDateTime {
        try {
            return ZonedDateTime.parse(value)
        } catch (ex: Exception) {
            throw InvalidEventException("captureTime must be specified")
        }
    }

    private fun isNullOrNegative(time: Long?) = time == null || time < 0
}
