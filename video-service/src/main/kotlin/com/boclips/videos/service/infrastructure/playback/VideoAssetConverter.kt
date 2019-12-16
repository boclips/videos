package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.flavorAsset.Asset
import com.boclips.videos.service.domain.model.video.VideoAsset
import com.boclips.videos.service.domain.model.video.VideoAssetId

object VideoAssetConverter {
    fun convert(asset: Asset): VideoAsset {
        return VideoAsset(
            id = VideoAssetId(asset.id),
            sizeKb = asset.sizeKb,
            width = asset.width,
            height = asset.height,
            bitrateKbps = asset.bitrateKbps
        )
    }
}