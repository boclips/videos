package com.boclips.videos.service.domain.model.playback

import java.time.Duration

class YoutubePlayback(
        val youtubeId: String,
        thumbnailUrl: String,
        duration: Duration
) : VideoPlayback(thumbnailUrl, duration)