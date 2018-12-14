package com.boclips.videos.service.domain.model.playback

import java.time.Duration

class YoutubePlayback(
        playbackId: PlaybackId,
        thumbnailUrl: String,
        duration: Duration
) : VideoPlayback(playbackId, thumbnailUrl, duration)