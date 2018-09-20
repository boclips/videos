package com.boclips.videos.service.application

import com.boclips.videos.service.infrastructure.event.Event
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.presentation.PlaybackEvent
import java.time.ZonedDateTime

class CreateEvent(private val eventService: EventService) {
    fun execute(event: PlaybackEvent?) {
        event ?: throw InvalidEventException("Event cannot be null")

        if (event.playerIdentifier.isNullOrBlank()) throw InvalidEventException("playerIdentifier must be specified")
        if (event.videoIdentifier.isNullOrBlank()) throw InvalidEventException("videoIdentifier must be specified")

        if (isNullOrNegative(event.segmentEndSeconds)) throw InvalidEventException("segmentEndSeconds must be specified")
        if (isNullOrNegative(event.segmentStartSeconds)) throw InvalidEventException("segmentStartSeconds must be specified")
        if (isNullOrNegative(event.videoDurationSeconds)) throw InvalidEventException("videoDurationSeconds must be specified")

        try {
            ZonedDateTime.parse(event.captureTime)
        } catch (ex: Exception) {
            throw InvalidEventException("captureTime must be specified")
        }

        eventService.saveEvent(Event("PLAYBACK", mapOf(
                "playerIdentifier" to event.playerIdentifier!!,
                "videoIdentifier" to event.videoIdentifier!!,
                "segmentStartSeconds" to event.segmentStartSeconds!!,
                "segmentEndSeconds" to event.segmentEndSeconds!!,
                "videoDurationSeconds" to event.videoDurationSeconds!!,
                "captureTime" to event.captureTime!!,
                "searchId" to event.searchId
        )))
    }

    private fun isNullOrNegative(time: Long?) = time == null || time < 0
}
