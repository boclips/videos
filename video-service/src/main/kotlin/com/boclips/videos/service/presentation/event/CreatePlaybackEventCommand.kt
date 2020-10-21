package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.application.analytics.InvalidEventException
import com.boclips.videos.service.common.isNullOrNegative
import java.time.ZonedDateTime

data class CreatePlaybackEventCommand(
    val videoId: String?,
    val videoIndex: Int?,
    val segmentStartSeconds: Long?,
    val segmentEndSeconds: Long?,
    val captureTime: ZonedDateTime? = null,
    val query: String? = null
) : EventCommand() {
    override fun isValidOrThrows() {
        if (this.videoId.isNullOrBlank()) throw InvalidEventException("videoId must be specified")

        if (isNullOrNegative(this.segmentEndSeconds)) throw InvalidEventException("segmentEndSeconds must be specified")
        if (isNullOrNegative(this.segmentStartSeconds)) throw InvalidEventException("segmentStartSeconds must be specified")
    }
}

