package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.flavorAsset.Asset
import com.boclips.videos.service.domain.model.playback.Dimensions
import com.boclips.videos.service.domain.model.video.VideoAsset

object VideoAssetConverter {
    fun convert(asset: Asset): VideoAsset {
        return VideoAsset(
            reference = asset.id,
            sizeKb = asset.sizeKb,
            dimensions = Dimensions(
                width = asset.width,
                height = asset.height
            ),
            bitrateKbps = asset.bitrateKbps
        )
    }
}
