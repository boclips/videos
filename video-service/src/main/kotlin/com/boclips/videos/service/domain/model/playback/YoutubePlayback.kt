package com.boclips.videos.service.domain.model.playback

import java.time.Duration

class YoutubePlayback(
        id: PlaybackId,
        thumbnailUrl: String,
        duration: Duration
) : VideoPlayback(id, thumbnailUrl, duration)