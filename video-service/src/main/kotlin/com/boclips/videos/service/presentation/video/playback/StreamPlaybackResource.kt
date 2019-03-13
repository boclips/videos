package com.boclips.videos.service.presentation.video.playback

class StreamPlaybackResource(
    type: String,
    val streamUrl: String,
    val downloadUrl: String
) : PlaybackResource(type)
