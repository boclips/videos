package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.playback.VideoPlayback

data class Video(
        val details: VideoDetails,
        val playback: VideoPlayback
)