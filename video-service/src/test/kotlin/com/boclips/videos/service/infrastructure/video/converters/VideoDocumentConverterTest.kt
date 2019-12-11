package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.VideoFactory.createVideoDocument
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
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
            contentPartnerName = "the contentPartner",
            contentPartnerVideoId = "the contentPartnerVideoId",
            contentPartner = ContentPartner(
                contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
                name = "Some name"
            ),
            videoReference = "video-123",
            type = ContentType.NEWS,
            keywords = listOf("keyword1", "keyword2"),
            subjects = setOf(TestFactories.createSubject(), TestFactories.createSubject()),
            releasedOn = LocalDate.ofYearDay(2018, 10),
            ingestedOn = LocalDate.ofYearDay(2019, 29),
            legalRestrictions = "legal restrictions",
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
            ratings = listOf(UserRating(3, UserId("user"))),
            ageRange = AgeRange.bounded(11, 16),
            distributionMethods = setOf(DistributionMethod.STREAM),
            promoted = true,
            shareCodes = setOf("1234")
        )

        val document = VideoDocumentConverter.toVideoDocument(originalAsset)
        val reconvertedAsset = VideoDocumentConverter.toVideo(document)

        assertThat(reconvertedAsset).isEqualTo(originalAsset)
    }

    @Test
    fun `converts null distributionMethods into ALL distribution methods`() {
        val videoDocument = createVideoDocument(distributionMethods = null)

        val video = VideoDocumentConverter.toVideo(videoDocument)

        assertThat(video.distributionMethods).isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `infers ingest date from the id when it is not known`() {
        val videoDocument = createVideoDocument(ingestDate = null, id = ObjectId("5de302800000000000000000"))

        val video = VideoDocumentConverter.toVideo(videoDocument)

        assertThat(video.ingestedOn).isEqualTo("2019-12-01")
    }
}
