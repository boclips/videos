package com.boclips.videos.service.domain.model.playback

import java.time.Duration

class StreamPlayback(
        val streamUrl: String,
        thumbnailUrl: String,
        duration: Duration
) : VideoPlayback(thumbnailUrl, duration)