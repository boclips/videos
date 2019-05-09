package com.boclips.videos.service.domain.model.playback

import java.time.Duration

class StreamPlayback(
    id: PlaybackId,
    thumbnailUrl: String,
    duration: Duration,
    val appleHlsStreamUrl: String,
    val downloadUrl: String
) : VideoPlayback(id = id, thumbnailUrl = thumbnailUrl, duration = duration) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as StreamPlayback

        if (appleHlsStreamUrl != other.appleHlsStreamUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + appleHlsStreamUrl.hashCode()
        return result
    }
}