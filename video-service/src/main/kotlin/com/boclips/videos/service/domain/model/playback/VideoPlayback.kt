package com.boclips.videos.service.domain.model.playback

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
        val appleHlsStreamUrl: String,
        val mpegDashStreamUrl: String,
        val progressiveDownloadStreamUrl: String,
        val downloadUrl: String
    ) : VideoPlayback()

    data class FaultyPlayback(
        override val id: PlaybackId,
        override val duration: Duration = Duration.ZERO
    ) : VideoPlayback()
}