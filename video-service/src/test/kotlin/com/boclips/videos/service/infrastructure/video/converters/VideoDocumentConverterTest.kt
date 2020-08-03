package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.contentwarning.ContentWarningId
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.Voice
import com.boclips.videos.service.domain.model.video.channel.Channel
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.AttachmentFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.VideoFactory.createVideoDocument
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale

class VideoDocumentConverterTest {
    @Test
    fun `converts a video to document to video`() {
        val originalVideo: Video = TestFactories.createVideo(
            videoId = "5c1786db5236de0001d77747",
            title = "the title",
            description = "the description",
            channelName = "the contentPartner",
            channelVideoId = "the contentPartnerVideoId",
            channel = Channel(
                channelId = ChannelId(
                    value = ObjectId().toHexString()
                ),
                name = "Some name"
            ),
            playback = TestFactories.createKalturaPlayback(),
            videoReference = "video-123",
            types = listOf(ContentType.NEWS, ContentType.INSTRUCTIONAL_CLIPS),
            keywords = listOf("keyword1", "keyword2"),
            subjects = setOf(TestFactories.createSubject(), TestFactories.createSubject()),
            releasedOn = LocalDate.ofYearDay(2018, 10),
            ingestedAt = ZonedDateTime.of(2019, 11, 12, 13, 14, 15, 160000000, ZoneOffset.UTC),
            legalRestrictions = "legal restrictions",
            voice = Voice.WithVoice(
                language = Locale.GERMANY,
                transcript = "hello"
            ),
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
            ratings = listOf(
                UserRating(
                    3,
                    UserId("user")
                )
            ),
            ageRange = AgeRange.of(min = 11, max = 16, curatedManually = true),
            promoted = true,
            contentWarnings = listOf(ContentWarning(id = ContentWarningId(ObjectId().toHexString()), label = "Warning 1")),
            deactivated = true,
            activeVideoId = TestFactories.createVideoId()
        )

        val document = VideoDocumentConverter.toVideoDocument(originalVideo)
        val recoveredVideo = VideoDocumentConverter.toVideo(document)

        assertThat(recoveredVideo).isEqualTo(originalVideo)
    }

    @Test
    fun `can convert attachments`() {
        val originalVideo: Video = TestFactories.createVideo(
            attachments = listOf(AttachmentFactory.sample())
        )

        val document = VideoDocumentConverter.toVideoDocument(originalVideo)
        val recoveredVideo = VideoDocumentConverter.toVideo(document)

        assertThat(recoveredVideo.attachments).isEqualTo(originalVideo.attachments)
    }

    @Nested
    inner class AgeRanges {
        @Test
        fun `can convert age ranges that have not been curated`() {
            val originalVideo: Video = TestFactories.createVideo(ageRange = AgeRange.of(3, 5, false))

            val document = VideoDocumentConverter.toVideoDocument(originalVideo)
            val recoveredVideo = VideoDocumentConverter.toVideo(document)

            assertThat(recoveredVideo.ageRange).isEqualTo(originalVideo.ageRange)
        }

        @Test
        fun `can convert age ranges that have been curated`() {
            val originalVideo: Video = TestFactories.createVideo(ageRange = AgeRange.of(3, 5, true))

            val document = VideoDocumentConverter.toVideoDocument(originalVideo)
            val recoveredVideo = VideoDocumentConverter.toVideo(document)

            assertThat(recoveredVideo.ageRange).isEqualTo(originalVideo.ageRange)
            assertThat(recoveredVideo.ageRange.curatedManually).isEqualTo(originalVideo.ageRange.curatedManually)
        }
    }

    @Test
    fun `infers ingest time from the id when it is not known`() {
        val videoDocument = createVideoDocument(ingestedAt = null, id = ObjectId("5de3ae1c0000000000000000"))

        val video = VideoDocumentConverter.toVideo(videoDocument)

        assertThat(video.ingestedAt).isEqualTo("2019-12-01T12:12:12Z")
    }

    @Nested
    inner class VoicedContent {
        @Test
        fun `can convert to with voiced`() {
            val videoDocument = createVideoDocument(id = ObjectId("5de3ae1c0000000000000000"), isVoiced = true)

            val video = VideoDocumentConverter.toVideo(videoDocument)

            assertThat(video.voice).isInstanceOf(Voice.WithVoice::class.java)
        }

        @Test
        fun `can convert to without voiced`() {
            val videoDocument = createVideoDocument(id = ObjectId("5de3ae1c0000000000000000"), isVoiced = false)

            val video = VideoDocumentConverter.toVideo(videoDocument)

            assertThat(video.voice).isInstanceOf(Voice.WithoutVoice::class.java)
        }

        @Test
        fun `can convert to unknown voiced`() {
            val videoDocument = createVideoDocument(id = ObjectId("5de3ae1c0000000000000000"), isVoiced = null)

            val video = VideoDocumentConverter.toVideo(videoDocument)

            assertThat(video.voice).isInstanceOf(Voice.UnknownVoice::class.java)
        }
    }
}
