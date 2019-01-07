package com.boclips.videos.service.infrastructure.video

import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.infrastructure.search.VideoMetadataConverter
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class VideoMetadataConverterTest {
    @Test
    fun convert() {
        val video = TestFactories.createVideoAsset(
                videoId = "123",
                title = "asset title",
                description = "asset description",
                contentProvider = "content partner",
                keywords = listOf("k1"),
                type = VideoType.VR_360_STOCK
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata).isEqualTo(VideoMetadata(
                id = "123",
                title = "asset title",
                description = "asset description",
                contentProvider = "content partner",
                keywords = listOf("k1"),
                typeId = 7,
                isEducational = true
        ))
    }

    @Test
    fun `it flags non-educational videos`() {
        val video = TestFactories.createVideoAsset(
                videoId = "123",
                title = "red carpet",
                type = VideoType.STOCK
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.isEducational).isEqualTo(false)
    }
}