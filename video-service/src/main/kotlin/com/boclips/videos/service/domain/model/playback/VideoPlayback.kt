package com.boclips.videos.service.domain.model.playback

import com.boclips.videos.service.domain.model.video.VideoAsset
import java.time.Duration
import java.time.ZonedDateTime

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
        val createdAt: ZonedDateTime?,
        val assets: Set<VideoAsset>?,
        val originalDimensions: Dimensions?,
        val thumbnailSecond: Int?,
        val customThumbnail: Boolean = false
    ) : VideoPlayback() {

        fun hasOriginalOrFHDResolution(): Boolean {
            return assets?.any { it.dimensions.isFHD() || it.dimensions.height == originalDimensions?.height } ?: false
        }

        fun hasAnyAssets() = assets == null || assets.isEmpty()
    }

    data class FaultyPlayback(
        override val id: PlaybackId,
        override val duration: Duration = Duration.ZERO
    ) : VideoPlayback()

    companion object {
        fun hasManuallySetThumbnail(playback: StreamPlayback): Boolean = playback.thumbnailSecond != null || playback.customThumbnail
    }
}
