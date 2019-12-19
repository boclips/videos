package com.boclips.videos.service.testsupport

import com.boclips.kalturaclient.captionasset.CaptionAsset
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.kalturaclient.flavorAsset.Asset

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

    fun createKalturaAsset(
        id: String = "1",
        bitrate: Int = 128,
        width: Int = 1920,
        height: Int = 1080,
        size: Int = 1024
    ): Asset {
        return Asset.builder()
            .id(id)
            .bitrateKbps(bitrate)
            .width(width)
            .height(height)
            .sizeKb(size)
            .build()
    }
}
