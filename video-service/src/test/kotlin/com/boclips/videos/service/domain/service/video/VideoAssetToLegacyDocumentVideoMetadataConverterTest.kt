package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class VideoToLegacyDocumentVideoMetadataConverterTest {

    @Test
    fun convert() {
        val videoId = TestFactories.aValidId()
        val video = TestFactories.createVideo(
            videoId = videoId,
            title = "the title",
            description = "the description",
            contentPartnerName = "Reuters",
            contentPartnerVideoId = "r001",
            playback = TestFactories.createKalturaPlayback(duration = Duration.ofSeconds(10)),
            type = ContentType.NEWS,
            keywords = listOf("keyword"),
            releasedOn = LocalDate.parse("2019-01-17")
        )

        val legacyMetadata = VideoToLegacyVideoMetadataConverter.convert(video)

        assertThat(legacyMetadata.id).isEqualTo(videoId)
        assertThat(legacyMetadata.title).isEqualTo("the title")
        assertThat(legacyMetadata.description).isEqualTo("the description")
        assertThat(legacyMetadata.keywords).containsExactly("keyword")
        assertThat(legacyMetadata.duration).isEqualTo(Duration.ofSeconds(10))
        assertThat(legacyMetadata.contentPartnerName).isEqualTo("Reuters")
        assertThat(legacyMetadata.contentPartnerVideoId).isEqualTo("r001")
        assertThat(legacyMetadata.releaseDate).isEqualTo(LocalDate.parse("2019-01-17"))
        assertThat(legacyMetadata.videoTypeTitle).isEqualTo(ContentType.NEWS.title)
    }
}
