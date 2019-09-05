package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.application.analytics.InvalidEventException

data class CreatePlayerInteractedWithEvent(
    val playerId: String?,
    val videoId: String?,
    val currentTime: Long?,
    val subtype: String?,
    val payload: Map<String, Any>?
) : EventCommand() {
    override fun isValidOrThrows() {
        if (this.playerId.isNullOrBlank()) throw InvalidEventException("playerId must be specified")
        if (this.videoId.isNullOrBlank()) throw InvalidEventException("videoId must be specified")
        if (this.currentTime === null) throw InvalidEventException("currentTime must be specified")
        if (this.subtype.isNullOrBlank()) throw InvalidEventException("subtype must be specified")
    }
}
