package com.boclips.videos.service.domain.model.playback

import java.time.Duration

abstract class VideoPlayback(
    val id: PlaybackId,
    val thumbnailUrl: String,
    val duration: Duration
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VideoPlayback

        if (id != other.id) return false
        if (thumbnailUrl != other.thumbnailUrl) return false
        if (duration != other.duration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + thumbnailUrl.hashCode()
        result = 31 * result + duration.hashCode()
        return result
    }
}