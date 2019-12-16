package com.boclips.videos.service.testsupport

import com.boclips.kalturaclient.captionasset.CaptionAsset
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.kalturaclient.flavorAsset.Asset
import com.boclips.videos.service.domain.model.video.VideoAssetId

object KalturaFactories {


    fun createKalturaCaptionAsset(
        language: KalturaLanguage = KalturaLanguage.ENGLISH,
        label: String = language.getName()
    ): CaptionAsset {
        return CaptionAsset.builder()
            .language(language)
            .label(label)
            .build()
    }

    fun createKalturaAsset(id: VideoAssetId = VideoAssetId("1"), bitrate: Int = 128, width: Int = 1920, height: Int = 1080, size: Int = 1024): Asset {
        return Asset.builder()
            .id(id.value)
            .bitrateKbps(bitrate)
            .width(width)
            .height(height)
            .sizeKb(size)
            .build()
    }
}