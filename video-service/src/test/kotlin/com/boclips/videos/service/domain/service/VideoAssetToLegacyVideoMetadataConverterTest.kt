package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class VideoAssetToLegacyVideoMetadataConverterTest {

    @Test
    fun convert() {
        val videoAssetId = TestFactories.aValidId()
        val asset = TestFactories.createVideoAsset(
                videoId = videoAssetId,
                title = "the title",
                description = "the description",
                keywords = listOf("keyword"),
                duration = Duration.ofSeconds(10),
                contentProvider = "Reuters",
                contentPartnerVideoId = "r001",
                releasedOn = LocalDate.parse("2019-01-17"),
                type = LegacyVideoType.TED_TALKS
        )

        val legacyMetadata = VideoAssetToLegacyVideoMetadataConverter.convert(asset)

        assertThat(legacyMetadata.id).isEqualTo(videoAssetId)
        assertThat(legacyMetadata.title).isEqualTo("the title")
        assertThat(legacyMetadata.description).isEqualTo("the description")
        assertThat(legacyMetadata.keywords).containsExactly("keyword")
        assertThat(legacyMetadata.duration).isEqualTo(Duration.ofSeconds(10))
        assertThat(legacyMetadata.contentPartnerName).isEqualTo("Reuters")
        assertThat(legacyMetadata.contentPartnerVideoId).isEqualTo("r001")
        assertThat(legacyMetadata.releaseDate).isEqualTo(LocalDate.parse("2019-01-17"))
        assertThat(legacyMetadata.videoTypeTitle).isEqualTo(LegacyVideoType.TED_TALKS.title)
    }
}