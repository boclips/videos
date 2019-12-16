package com.boclips.videos.service.domain.model.playback

import com.boclips.videos.service.domain.model.video.VideoAsset
import java.time.Duration

sealed class VideoPlayback {
    abstract val id: PlaybackId
    abstract val duration: Duration

    data class YoutubePlayback(
        override val id: PlaybackId,
        override val duration: Duration,
        val thumbnailUrl: String
    ) : VideoPlayback()

    data class StreamPlayback(
        override val id: PlaybackId,
        override val duration: Duration,
        val referenceId: String,
        val downloadUrl: String,
        val assets: Set<VideoAsset>?
    ) : VideoPlayback()

    data class FaultyPlayback(
        override val id: PlaybackId,
        override val duration: Duration = Duration.ZERO
    ) : VideoPlayback()
}
