package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class VideoDocumentConverterTest {

    @Test
    fun `converts a video to a document`() {
        val originalAsset = TestFactories.createVideoAsset(
            videoId = "5c1786db5236de0001d77747",
            title = "the title",
            description = "the description",
            contentProvider = "the contentPartner",
            contentPartnerVideoId = "the contentPartnerVideoId",
            playbackId = PlaybackId(PlaybackProviderType.KALTURA, "the playbackId"),
            type = LegacyVideoType.NEWS,
            keywords = listOf("keyword1", "keyword2"),
            subjects = setOf(Subject("subject1"), Subject("subject2")),
            releasedOn = LocalDate.ofYearDay(2018, 10),
            duration = Duration.ofSeconds(97),
            language = "pl-PL",
            transcript = "hello",
            legalRestrictions = "legal restrictions"
        )

        val document = VideoDocumentConverter.toDocument(originalAsset)
        val reconvertedAsset = VideoDocumentConverter.toAsset(document)

        assertThat(reconvertedAsset).isEqualTo(originalAsset)
    }
}
