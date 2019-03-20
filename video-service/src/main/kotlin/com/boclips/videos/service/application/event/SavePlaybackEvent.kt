package com.boclips.videos.service.application.event

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.infrastructure.analytics.AnalyticsEventService
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand

class SavePlaybackEvent(
    private val analyticsEventService: AnalyticsEventService
) {
    fun execute(event: CreatePlaybackEventCommand?) {

        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        analyticsEventService.savePlaybackEvent(
            playerId = event.playerId!!,
            videoId = AssetId(event.videoId!!),
            videoIndex = event.videoIndex,
            segmentStartSeconds = event.segmentStartSeconds!!,
            segmentEndSeconds = event.segmentEndSeconds!!,
            videoDurationSeconds = event.videoDurationSeconds!!
        )
    }
}
