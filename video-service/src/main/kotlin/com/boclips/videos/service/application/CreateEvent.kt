package com.boclips.videos.service.application

import com.boclips.videos.service.infrastructure.event.Event
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.presentation.CreatePlaybackEventCommand
import java.time.ZonedDateTime

data class PlaybackEventData(
        val playerId: String,
        val videoId: String,
        val segmentStartSeconds: Long,
        val segmentEndSeconds: Long,
        val videoDurationSeconds: Long,
        val searchId: String?
)

class PlaybackEvent(
        playerId: String,
        videoId: String,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
        videoDurationSeconds: Long,
        captureTime: ZonedDateTime,
        searchId: String?
) : Event<PlaybackEventData>("PLAYBACK", captureTime, PlaybackEventData(
        playerId = playerId,
        videoId = videoId,
        segmentStartSeconds = segmentStartSeconds,
        segmentEndSeconds = segmentEndSeconds,
        videoDurationSeconds = videoDurationSeconds,
        searchId = searchId
))

class CreateEvent(private val eventService: EventService) {
    fun execute(event: CreatePlaybackEventCommand?) {
        event ?: throw InvalidEventException("Event cannot be null")

        if (event.playerId.isNullOrBlank()) throw InvalidEventException("playerId must be specified")
        if (event.videoId.isNullOrBlank()) throw InvalidEventException("videoId must be specified")

        if (isNullOrNegative(event.segmentEndSeconds)) throw InvalidEventException("segmentEndSeconds must be specified")
        if (isNullOrNegative(event.segmentStartSeconds)) throw InvalidEventException("segmentStartSeconds must be specified")
        if (isNullOrNegative(event.videoDurationSeconds)) throw InvalidEventException("videoDurationSeconds must be specified")

        eventService.saveEvent(PlaybackEvent(
                playerId = event.playerId!!,
                videoId = event.videoId!!,
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
