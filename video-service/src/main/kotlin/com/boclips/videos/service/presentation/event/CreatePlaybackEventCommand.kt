package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.application.event.InvalidEventException

data class CreatePlaybackEventCommand(
        val playerId: String?,
        val videoId: String?,
        val segmentStartSeconds: Long?,
        val segmentEndSeconds: Long?,
        val videoDurationSeconds: Long?,
        val captureTime: String?,
        val searchId: String?
) : EventCommand() {
    override fun isValidOrThrows() {
        if (this.playerId.isNullOrBlank()) throw InvalidEventException("playerId must be specified")
        if (this.videoId.isNullOrBlank()) throw InvalidEventException("assetId must be specified")

        if (isNullOrNegative(this.segmentEndSeconds)) throw InvalidEventException("segmentEndSeconds must be specified")
        if (isNullOrNegative(this.segmentStartSeconds)) throw InvalidEventException("segmentStartSeconds must be specified")
        if (isNullOrNegative(this.videoDurationSeconds)) throw InvalidEventException("videoDurationSeconds must be specified")
    }
}

private fun isNullOrNegative(time: Long?) = time == null || time < 0
