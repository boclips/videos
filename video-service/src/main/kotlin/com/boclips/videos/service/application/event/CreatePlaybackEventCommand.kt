package com.boclips.videos.service.application.event

data class CreatePlaybackEventCommand(
        val playerId: String?,
        val videoId: String?,
        val segmentStartSeconds: Long?,
        val segmentEndSeconds: Long?,
        val videoDurationSeconds: Long?,
        val captureTime: String?,
        val searchId: String?
)
