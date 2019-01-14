package com.boclips.videos.service.domain.service

import com.boclips.search.service.domain.legacy.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class VideoAssetToLegacyVideoMetadataConverterTest {

    @Test
    fun convert() {
        val asset = TestFactories.createVideoAsset(
                videoId = "1",
                title = "the title",
                description = "the description",
                keywords = listOf("keyword"),
                duration = Duration.ofSeconds(10),
                contentProvider = "Reuters",
                contentPartnerVideoId = "r001",
                releasedOn = LocalDate.parse("2019-01-17"),
                type = VideoType.TED_TALKS
        )

        val legacyMetadata = VideoAssetToLegacyVideoMetadataConverter.convert(asset)

        assertThat(legacyMetadata.id).isEqualTo("1")
        assertThat(legacyMetadata.title).isEqualTo("the title")
        assertThat(legacyMetadata.description).isEqualTo("the description")
        assertThat(legacyMetadata.keywords).containsExactly("keyword")
        assertThat(legacyMetadata.duration).isEqualTo(Duration.ofSeconds(10))
        assertThat(legacyMetadata.contentPartnerName).isEqualTo("Reuters")
        assertThat(legacyMetadata.contentPartnerVideoId).isEqualTo("r001")
        assertThat(legacyMetadata.releaseDate).isEqualTo(LocalDate.parse("2019-01-17"))
        assertThat(legacyMetadata.videoType).isEqualTo(LegacyVideoType.TED_TALKS)
    }

    @Test
    fun convertVideoType() {
        assertThat(VideoAssetToLegacyVideoMetadataConverter.convertVideoType(VideoType.STOCK)).isEqualTo(LegacyVideoType.STOCK)
        assertThat(VideoAssetToLegacyVideoMetadataConverter.convertVideoType(VideoType.TED_ED)).isEqualTo(LegacyVideoType.TED_ED)
    }
}