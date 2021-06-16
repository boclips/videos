package com.boclips.videos.service.domain.model.playback

import com.boclips.videos.service.domain.model.video.VideoAsset
import com.boclips.videos.service.testsupport.TestFactories.createKalturaPlayback
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoPlaybackTest {

    @Test
    fun `when FHD resolution`() {
        val playback = createKalturaPlayback(
            originalDimensions = Dimensions(9999, 9999),
            assets = setOf(
                videoAssetWithHeight(10),
                videoAssetWithHeight(1080),
                videoAssetWithHeight(10)
            )
        )

        assertThat(playback.hasOriginalOrFHDResolution()).isTrue()
    }

    @Test
    fun `when original resolution`() {
        val playback = createKalturaPlayback(
            originalDimensions = Dimensions(10, 10),
            assets = setOf(
                videoAssetWithHeight(10)
            )
        )

        assertThat(playback.hasOriginalOrFHDResolution()).isTrue()
    }

    @Test
    fun `when low resolution asset`() {
        val playback = createKalturaPlayback(
            originalDimensions = Dimensions(1000, 2000),
            assets = setOf(
                videoAssetWithHeight(100)
            )
        )

        assertThat(playback.hasOriginalOrFHDResolution()).isFalse()
    }

    private fun videoAssetWithHeight(height: Int): VideoAsset {
        return VideoAsset(
            reference = "",
            sizeKb = 0,
            dimensions = Dimensions(width = 10, height = height),
            bitrateKbps = 0
        )
    }
}
