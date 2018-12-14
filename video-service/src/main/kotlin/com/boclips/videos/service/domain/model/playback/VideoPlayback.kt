package com.boclips.videos.service.domain.model.playback

import java.time.Duration

abstract class VideoPlayback(
        val id: PlaybackId,
        val thumbnailUrl: String,
        val duration: Duration
)