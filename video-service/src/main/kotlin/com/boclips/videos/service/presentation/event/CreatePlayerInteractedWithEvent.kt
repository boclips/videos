package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.application.analytics.InvalidEventException
import com.boclips.videos.service.common.isNullOrNegative

data class CreatePlayerInteractedWithEvent(
    val playerId: String?,
    val videoId: String?,
    val videoDurationSeconds: Long?,
    val currentTime: Long?,
    val type: String?,
    val payload: Map<String, Any>?
) : EventCommand() {
    override fun isValidOrThrows() {
        if (this.playerId.isNullOrBlank()) throw InvalidEventException("playerId must be specified")
        if (this.videoId.isNullOrBlank()) throw InvalidEventException("videoId must be specified")
        if (this.videoDurationSeconds === null) throw InvalidEventException("videoDurationSeconds must be specified")
        if (this.currentTime === null) throw InvalidEventException("currentTime must be specified")
        if (this.type.isNullOrBlank()) throw InvalidEventException("type must be specified")

        if (isNullOrNegative(this.videoDurationSeconds)) throw InvalidEventException("videoDurationSeconds must be specified")
    }
}
