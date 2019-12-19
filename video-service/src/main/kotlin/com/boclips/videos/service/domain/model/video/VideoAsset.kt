package com.boclips.videos.service.domain.model.video

data class VideoAsset(
        val reference: String,
        val sizeKb: Int,
        val dimensions: Dimensions,
        val bitrateKbps: Int
)
