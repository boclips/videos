package com.boclips.videos.service.application.event

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand

class SavePlaybackEvent(
        private val eventService: EventService
) {
    fun execute(event: CreatePlaybackEventCommand?) {

        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        eventService.savePlaybackEvent(
                playerId = event.playerId!!,
                videoId = AssetId(event.assetId!!),
                videoIndex = event.videoIndex,
                segmentStartSeconds = event.segmentStartSeconds!!,
                segmentEndSeconds = event.segmentEndSeconds!!,
                videoDurationSeconds = event.videoDurationSeconds!!
        )
    }


}
