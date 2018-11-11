package com.boclips.videos.service.domain.model.playback

import java.time.Duration

class StreamPlayback(
        playbackId: PlaybackId,
        thumbnailUrl: String,
        duration: Duration,
        val streamUrl: String
) : VideoPlayback(playbackId = playbackId, thumbnailUrl = thumbnailUrl, duration = duration)