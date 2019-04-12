package com.boclips.videos.service.infrastructure.video

import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.infrastructure.search.VideoMetadataConverter
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class VideoMetadataConverterTest {
    @Test
    fun `convert instructional video`() {
        val videoAssetId = TestFactories.aValidId()

        val video = TestFactories.createVideoAsset(
            videoId = videoAssetId,
            title = "asset title",
            description = "asset description",
            contentPartnerId = "content partner",
            releasedOn = LocalDate.of(2019, Month.APRIL, 19),
            keywords = listOf("k1"),
            type = LegacyVideoType.INSTRUCTIONAL_CLIPS
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata).isEqualTo(
            VideoMetadata(
                id = videoAssetId,
                title = "asset title",
                description = "asset description",
                contentProvider = "content partner",
                releaseDate = LocalDate.of(2019, Month.APRIL, 19),
                keywords = listOf("k1"),
                tags = listOf("classroom")
            )
        )
    }

    @Test
    fun `tags classroom video`() {
        val video = TestFactories.createVideoAsset(
            type = LegacyVideoType.INSTRUCTIONAL_CLIPS
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).containsExactly("classroom")
    }

    @Test
    fun `tags news video`() {
        val video = TestFactories.createVideoAsset(
            type = LegacyVideoType.NEWS
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).contains("news")
    }

    @Test
    fun `it can apply multiple tags`() {
        val video = TestFactories.createVideoAsset(
            type = LegacyVideoType.NEWS,
            description = "biology animation"
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).containsExactly("classroom", "news")
    }

    @Test
    fun `it doesn't tag videos without a match`() {
        val video = TestFactories.createVideoAsset(
            videoId = TestFactories.aValidId(),
            title = "garbage title",
            type = LegacyVideoType.STOCK
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).isEmpty()
    }
}
