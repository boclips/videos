package com.boclips.videos.service.infrastructure.video.mongo.converters

import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.video.LegacySubject
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Locale

class VideoDocumentConverterTest {
    @Test
    fun `converts a video to document to video`() {
        val originalAsset = TestFactories.createVideo(
            videoId = "5c1786db5236de0001d77747",
            title = "the title",
            description = "the description",
            contentPartnerId = "the contentPartner",
            contentPartnerVideoId = "the contentPartnerVideoId",
            type = LegacyVideoType.NEWS,
            keywords = listOf("keyword1", "keyword2"),
            subjects = setOf(LegacySubject("subject1"), LegacySubject("subject2")),
            releasedOn = LocalDate.ofYearDay(2018, 10),
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
            legalRestrictions = "legal restrictions",
            ageRange = AgeRange.bounded(11, 16)
        )

        val document = VideoDocumentConverter.toVideoDocument(originalAsset)
        val reconvertedAsset = VideoDocumentConverter.toVideo(document)

        assertThat(reconvertedAsset).isEqualTo(originalAsset)
    }
}
