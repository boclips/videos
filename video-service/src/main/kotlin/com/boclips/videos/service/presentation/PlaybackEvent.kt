package com.boclips.videos.service.presentation

data class PlaybackEvent(
        val playerIdentifier: String?,
        val videoIdentifier: String?,
        val segmentStartSeconds: Long?,
        val segmentEndSeconds: Long?,
        val videoDurationSeconds: Long?,
        val captureTime: String?
)
