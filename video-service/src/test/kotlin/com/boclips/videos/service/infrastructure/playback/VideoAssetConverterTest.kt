package com.boclips.videos.service.infrastructure.playback

import com.boclips.videos.service.testsupport.KalturaFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoAssetConverterTest() {
    @Test
    fun convert() {
        val asset = VideoAssetConverter.convert(
            KalturaFactories.createKalturaAsset(
                id = "abc",
                bitrate = 124,
                width = 480,
                height = 320,
                size = 1024
            )
        )

        assertThat(asset.reference).isEqualTo("abc")
        assertThat(asset.bitrateKbps).isEqualTo(124)
        assertThat(asset.dimensions.width).isEqualTo(480)
        assertThat(asset.dimensions.height).isEqualTo(320)
        assertThat(asset.sizeKb).isEqualTo(1024)
    }
}
