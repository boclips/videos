package com.boclips.videos.service.presentation.video.playback

import java.time.Duration

sealed class PlaybackResource {
    abstract var id: String?
    abstract var thumbnailUrl: String?
    abstract var duration: Duration?
    abstract val type: String
}

data class StreamPlaybackResource(
    override val type: String = "STREAM",
    override var id: String?,
    override var thumbnailUrl: String?,
    override var duration: Duration?,
    val streamUrl: String,
    val referenceId: String
) : PlaybackResource()

data class YoutubePlaybackResource(
    override val type: String = "YOUTUBE",
    override var id: String?,
    override var thumbnailUrl: String?,
    override var duration: Duration?
) : PlaybackResource()
