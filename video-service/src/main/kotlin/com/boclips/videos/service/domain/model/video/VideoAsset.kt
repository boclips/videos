package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.playback.Dimensions

data class VideoAsset(
    val reference: String,
    val sizeKb: Int,
    val dimensions: Dimensions,
    val bitrateKbps: Int
)
