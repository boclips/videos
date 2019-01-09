package com.boclips.videos.service.infrastructure.video

import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.infrastructure.search.VideoMetadataConverter
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class VideoMetadataConverterTest {
    @Test
    fun `convert instructional video`() {
        val video = TestFactories.createVideoAsset(
                videoId = "123",
                title = "asset title",
                description = "asset description",
                contentProvider = "content partner",
                keywords = listOf("k1"),
                type = VideoType.INSTRUCTIONAL_CLIPS
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata).isEqualTo(VideoMetadata(
                id = "123",
                title = "asset title",
                description = "asset description",
                contentProvider = "content partner",
                keywords = listOf("k1"),
                tags = listOf("classroom")
        ))
    }

    @Test
    fun `tags classroom video`() {
        val video = TestFactories.createVideoAsset(
                type = VideoType.INSTRUCTIONAL_CLIPS
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).containsExactly("classroom")
    }

    @Test
    fun `tags news video`() {
        val video = TestFactories.createVideoAsset(
                type = VideoType.NEWS
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).contains("news")
    }

    @Test
    fun `it can apply multiple tags`() {
        val video = TestFactories.createVideoAsset(
                type = VideoType.NEWS,
                description = "biology animation"
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).containsExactly("classroom", "news")
    }

    @Test
    fun `it doesn't tag videos without a match`() {
        val video = TestFactories.createVideoAsset(
                videoId = "123",
                title = "garbage title",
                type = VideoType.STOCK
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).isEmpty()
    }
}