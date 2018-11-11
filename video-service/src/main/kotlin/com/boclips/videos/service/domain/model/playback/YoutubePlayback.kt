package com.boclips.videos.service.domain.model.playback

import java.time.Duration

class YoutubePlayback(
        playbackId: PlaybackId,
        thumbnailUrl: String,
        duration: Duration,
        val youtubeId: String
) : VideoPlayback(playbackId, thumbnailUrl, duration)