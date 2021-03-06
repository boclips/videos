package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.SubjectsMetadata
import com.boclips.search.service.domain.videos.model.VideoCategoryCodes
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.user.OrganisationId
import com.boclips.videos.service.domain.model.video.Price
import com.boclips.videos.service.domain.model.video.Transcript
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.Voice
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.model.video.channel.ContentPartnerAvailability
import com.boclips.videos.service.testsupport.AttachmentFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserRatingFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Currency
import java.util.Locale
import com.boclips.search.service.domain.videos.model.VideoType as SearchVideoType

class VideoMetadataConverterTest {
    @Test
    fun `convert instructional video`() {
        val videoId = TestFactories.aValidId()

        val video = TestFactories.createVideoWithPrices(
            TestFactories.createVideo(
                videoId = videoId,
                title = "video title",
                description = "video description",
                channelName = "content partner",
                channelId = ChannelId(
                    "content-partner-id"
                ),
                playback = TestFactories.createYoutubePlayback(
                    duration = Duration.ofSeconds(10)
                ),
                updatedAt = ZonedDateTime.parse("2021-07-12T09:30Z[UTC]"),
                types = listOf(VideoType.INSTRUCTIONAL_CLIPS),
                keywords = listOf("k1"),
                releasedOn = LocalDate.of(2019, Month.APRIL, 19),
                voice = Voice.WithVoice(language = Locale.JAPANESE, transcript = Transcript(content = "a great transcript", isHumanGenerated = false, isRequested = false)),
                ageRange = AgeRange.of(min = 5, max = 11, curatedManually = true),
                categories = mapOf(
                    CategorySource.CHANNEL to setOf(
                        CategoryWithAncestors(
                            codeValue = CategoryCode("ABC"),
                            description = "Test",
                            ancestors = setOf(CategoryCode("A"), CategoryCode("AB"))
                        )
                    )
                ),
                subjects = setOf(
                    Subject(
                        id = SubjectId(value = "subject-id"),
                        name = "subject name"
                    )
                ),
                subjectsSetManually = true,
                promoted = true,
                ratings = emptyList(),
                attachments = listOf(AttachmentFactory.sample(type = AttachmentType.ACTIVITY)),
                ingestedAt = ZonedDateTime.of(2018, 12, 10, 0, 0, 0, 0, ZoneOffset.UTC)
            ),
            prices = mapOf(
                OrganisationId("org-1") to Price(
                    amount = BigDecimal.valueOf(9.99),
                    currency = Currency.getInstance("USD")
                ),
                OrganisationId("org-2") to Price(
                    amount = BigDecimal.valueOf(15.99),
                    currency = Currency.getInstance("USD")
                ),
            ),
        )

        val videoMetadata = VideoMetadataConverter.convert(video, ContentPartnerAvailability.NONE)

        assertThat(videoMetadata).isEqualTo(
            VideoMetadata(
                id = videoId,
                title = "video title",
                rawTitle = "video title",
                description = "video description",
                contentProvider = "content partner",
                contentPartnerId = "content-partner-id",
                releaseDate = LocalDate.of(2019, Month.APRIL, 19),
                updatedAt = ZonedDateTime.parse("2021-07-12T09:30Z[UTC]"),
                keywords = listOf("k1"),
                tags = emptyList(),
                durationSeconds = 10,
                source = SourceType.YOUTUBE,
                transcript = "a great transcript",
                isVoiced = true,
                language = Locale.JAPANESE,
                ageRangeMin = 5,
                ageRangeMax = 11,
                subjects = SubjectsMetadata(
                    items = setOf(SubjectMetadata(id = "subject-id", name = "subject name")),
                    setManually = true
                ),
                promoted = true,
                meanRating = null,
                eligibleForStream = false,
                eligibleForDownload = false,
                attachmentTypes = setOf("Activity"),
                deactivated = false,
                types = listOf(SearchVideoType.INSTRUCTIONAL),
                ingestedAt = ZonedDateTime.of(2018, 12, 10, 0, 0, 0, 0, ZoneOffset.UTC),
                prices = mapOf(
                    "DEFAULT" to BigDecimal.valueOf(25),
                    "org-1" to BigDecimal.valueOf(9.99),
                    "org-2" to BigDecimal.valueOf(15.99)
                ),
                categoryCodes = VideoCategoryCodes(codes = listOf("A", "AB", "ABC"))
            )
        )
    }

    @Test
    fun `tags news video`() {
        val video = TestFactories.createVideoWithPrices(
            TestFactories.createVideo(
                types = listOf(VideoType.NEWS)
            )
        )

        val videoMetadata = VideoMetadataConverter.convert(video, ContentPartnerAvailability.ALL)

        assertThat(videoMetadata.tags).contains("news")
    }

    @Test
    fun `it doesn't tag videos without a match`() {
        val video = TestFactories.createVideoWithPrices(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                title = "garbage title",
                types = listOf(VideoType.STOCK)
            )
        )

        val videoMetadata = VideoMetadataConverter.convert(video, ContentPartnerAvailability.ALL)

        assertThat(videoMetadata.tags).isEmpty()
    }

    @Test
    fun `converts youtube playback to youtube source type`() {
        val video = TestFactories.createVideoWithPrices(
            TestFactories.createVideo(
                playback = TestFactories.createYoutubePlayback(
                    playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "123")
                )
            )
        )

        val videoMetadata = VideoMetadataConverter.convert(video, ContentPartnerAvailability.ALL)

        assertThat(videoMetadata.source).isEqualTo(SourceType.YOUTUBE)
    }

    @Test
    fun `converts kaltura playback to boclips source type`() {
        val video = TestFactories.createVideoWithPrices(
            TestFactories.createVideo(
                playback = TestFactories.createKalturaPlayback(
                    referenceId = "123"
                )
            )
        )

        val videoMetadata = VideoMetadataConverter.convert(video, ContentPartnerAvailability.ALL)

        assertThat(videoMetadata.source).isEqualTo(SourceType.BOCLIPS)
    }

    @Test
    fun `aggregates ratings for video when converting`() {
        val ratings = listOf(3, 5, 4, 2, 4)

        val video = TestFactories.createVideoWithPrices(
            TestFactories.createVideo(
                ratings = ratings.map { UserRatingFactory.sample(rating = it) }
            )
        )

        val videoMetadata = VideoMetadataConverter.convert(video, ContentPartnerAvailability.ALL)

        assertThat(videoMetadata.meanRating).isCloseTo(
            ratings.sum() / ratings.size.toDouble(),
            Offset.offset(0.00001)
        )
    }

    @Test
    fun `keeps tags explicitly set on the video`() {
        val video = TestFactories.createVideoWithPrices(
            TestFactories.createVideo(
                types = listOf(VideoType.NEWS),
                tags = listOf(TestFactories.createUserTag(label = "explainer"))
            )
        )

        val videoMetadata = VideoMetadataConverter.convert(video, ContentPartnerAvailability.ALL)

        assertThat(videoMetadata.tags).containsExactlyInAnyOrder("news", "explainer")
    }

    @Test
    fun `videos are eligible for streaming based on their content partner`() {
        val video = TestFactories.createVideoWithPrices(TestFactories.createVideo())

        val videoMetadata = VideoMetadataConverter.convert(video, ContentPartnerAvailability.STREAMING)

        // The compiler is lying. You know nothing, IntelliJ (or maybe I know nothing?)
        assertThat(videoMetadata.eligibleForStream).isTrue()
        assertThat(videoMetadata.eligibleForDownload).isFalse()
    }

    @Test
    fun `videos eligible for all based on their content partner`() {
        val video = TestFactories.createVideoWithPrices(TestFactories.createVideo())

        val videoMetadata = VideoMetadataConverter.convert(video, ContentPartnerAvailability.ALL)

        assertThat(videoMetadata.eligibleForStream).isTrue()
        assertThat(videoMetadata.eligibleForDownload).isTrue()
    }

    @Test
    fun `can handle null prices`() {
        val videoWithNoPrices = TestFactories.createVideo()
        val metadata = VideoMetadataConverter.convert(videoWithNoPrices, ContentPartnerAvailability.ALL)
        assertThat(metadata.prices).isNull()
    }

    @Test
    fun `categories are ordered alphabetically`() {
        val video = TestFactories.createVideoWithPrices(
            TestFactories.createVideo(
                categories = mapOf(
                    CategorySource.CHANNEL to setOf(
                        CategoryWithAncestors(
                            codeValue = CategoryCode("ABC"),
                            description = "Test",
                            ancestors = setOf(CategoryCode("A"), CategoryCode("AB"))
                        )
                    ),
                    CategorySource.MANUAL to setOf(
                        CategoryWithAncestors(
                            codeValue = CategoryCode("BBB"),
                            description = "B test",
                            ancestors = setOf(CategoryCode("B"), CategoryCode("BB"))
                        )
                    )
                ),
            )
        )

        val metadata = VideoMetadataConverter.convert(video, ContentPartnerAvailability.ALL)
        assertThat(metadata.categoryCodes?.codes).containsExactly("A", "AB", "ABC", "B", "BB", "BBB")
    }
}
