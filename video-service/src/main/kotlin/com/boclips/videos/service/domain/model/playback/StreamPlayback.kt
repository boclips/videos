package com.boclips.videos.service.domain.model.playback

import java.time.Duration

class StreamPlayback(
        id: PlaybackId,
        thumbnailUrl: String,
        duration: Duration,
        val streamUrl: String
) : VideoPlayback(id = id, thumbnailUrl = thumbnailUrl, duration = duration)