package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.playback.VideoPlayback

data class Video(
    val asset: VideoAsset,
    val playback: VideoPlayback
)