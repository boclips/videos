package com.boclips.videos.service.domain.model

import java.time.Duration

class VideoPlayback(
        val streamUrl: String,
        val thumbnailUrl: String,
        val duration: Duration
)