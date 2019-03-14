package com.boclips.videos.service.presentation.video.playback

class StreamPlaybackResource(
    type: String,
    val streamUrl: String
) : PlaybackResource(type)
