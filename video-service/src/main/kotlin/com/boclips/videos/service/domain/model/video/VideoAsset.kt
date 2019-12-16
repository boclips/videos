package com.boclips.videos.service.domain.model.video

data class VideoAssetId(val value: String)

data class VideoAsset(
        val id: VideoAssetId,
        val sizeKb: Int,
        val width: Int,
        val height: Int,
        val bitrateKbps: Int
)