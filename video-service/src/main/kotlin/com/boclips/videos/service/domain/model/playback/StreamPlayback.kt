package com.boclips.videos.service.domain.model.playback

import java.time.Duration

class StreamPlayback(
    id: PlaybackId,
    thumbnailUrl: String,
    duration: Duration,
    val streamUrl: String
) : VideoPlayback(id = id, thumbnailUrl = thumbnailUrl, duration = duration) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as StreamPlayback

        if (streamUrl != other.streamUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + streamUrl.hashCode()
        return result
    }
}