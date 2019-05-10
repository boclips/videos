package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.domain.model.asset.LegacySubject
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.Topic
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.util.Locale

class VideoDocumentConverterTest {

    @Test
    fun `converts a video to a document`() {
        val originalAsset = TestFactories.createVideoAsset(
            videoId = "5c1786db5236de0001d77747",
            title = "the title",
            description = "the description",
            contentPartnerId = "the contentPartner",
            contentPartnerVideoId = "the contentPartnerVideoId",
            playbackId = PlaybackId(PlaybackProviderType.KALTURA, "the playbackId"),
            type = LegacyVideoType.NEWS,
            keywords = listOf("keyword1", "keyword2"),
            subjects = setOf(LegacySubject("subject1"), LegacySubject("subject2")),
            releasedOn = LocalDate.ofYearDay(2018, 10),
            duration = Duration.ofSeconds(97),
            language = Locale.GERMANY,
            transcript = "hello",
            topics = setOf(
                Topic(
                    name = "topic name",
                    language = Locale.forLanguageTag("es-ES"),
                    confidence = 0.23,
                    parent = Topic(
                        name = "parent topic",
                        parent = null,
                        language = Locale.forLanguageTag("es-ES"),
                        confidence = 1.0
                    )
                )
            ),
            legalRestrictions = "legal restrictions"
        )

        val document = VideoDocumentConverter.toVideoDocument(originalAsset)
        val reconvertedAsset = VideoDocumentConverter.toVideoAsset(document)

        assertThat(reconvertedAsset).isEqualTo(originalAsset)
    }

//    @Test
//    @Disabled
//    fun `converts a Kaltura playback to a document`() {
//        val originalPlayback = TestFactories.createKalturaPlayback()
//
//        val playbackDocument = VideoDocumentConverter.toPlaybackDocument(originalPlayback)
//        assertThat(playbackDocument.id).isEqualTo("xxx")
//        assertThat(playbackDocument.thumbnailUrl).isEqualTo("xxx")
//        assertThat(playbackDocument.downloadUrl).isEqualTo("xxx")
//        assertThat(playbackDocument.hdsStreamUrl).isEqualTo("xxx")
//        assertThat(playbackDocument.dashStreamUrl).isEqualTo("xxx")
//        assertThat(playbackDocument.progressiveStreamUrl).isEqualTo("xxx")
//        assertThat(playbackDocument.duration).isEqualTo("xxx")
//        assertThat(playbackDocument.lastVerified).isEqualTo("xxx")
//
//        val convertedPlayback = VideoDocumentConverter.toPlayback(playbackDocument)
//        assertThat(convertedPlayback).isEqualTo(originalPlayback)
//    }
}
