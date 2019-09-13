package com.boclips.videos.service.domain.model.playback

import java.time.Duration

sealed class VideoPlayback {
    abstract val id: PlaybackId
    abstract val thumbnailUrl: String
    abstract val duration: Duration

    data class YoutubePlayback(
        override val id: PlaybackId,
        override val thumbnailUrl: String,
        override val duration: Duration
    ) : VideoPlayback()

    data class StreamPlayback(
        override val id: PlaybackId,
        override val thumbnailUrl: String,
        override val duration: Duration,
        val entryId: String?,
        val appleHlsStreamUrl: String,
        val mpegDashStreamUrl: String,
        val progressiveDownloadStreamUrl: String,
        val downloadUrl: String
    ) : VideoPlayback()

    data class FaultyPlayback(
        override val id: PlaybackId,
        override val thumbnailUrl: String = "",
        override val duration: Duration = Duration.ZERO
    ) : VideoPlayback()
}