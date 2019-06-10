package com.boclips.videos.service.domain.model.playback

sealed class VideoProviderMetadata {
    abstract val id: PlaybackId

    data class YoutubeMetadata(
        override val id: PlaybackId,
        val channelId: String,
        val channelName: String
    ) : VideoProviderMetadata()

    data class KalturaMetadata(
        override val id: PlaybackId
    ) : VideoProviderMetadata()
}
