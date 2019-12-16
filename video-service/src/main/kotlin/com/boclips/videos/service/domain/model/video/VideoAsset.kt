package com.boclips.videos.service.domain.model.video

data class VideoAsset(
        val sizeKb: Int,
        val width: Int,
        val height: Int,
        val bitrateKbps: Int
)