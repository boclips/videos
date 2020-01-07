package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.SubjectsMetadata
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserRatingFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.Month

class VideoMetadataConverterTest {
    @Test
    fun `convert instructional video`() {
        val videoId = TestFactories.aValidId()

        val video = TestFactories.createVideo(
            videoId = videoId,
            title = "video title",
            description = "video description",
            contentPartnerName = "content partner",
            playback = TestFactories.createYoutubePlayback(
                duration = Duration.ofSeconds(10)
            ),
            type = ContentType.INSTRUCTIONAL_CLIPS,
            keywords = listOf("k1"),
            releasedOn = LocalDate.of(2019, Month.APRIL, 19),
            transcript = "a great transcript",
            ageRange = AgeRange.bounded(5, 11),
            subjects = setOf(
                Subject(
                    id = SubjectId(value = "subject-id"),
                    name = "subject name"
                )
            ),
            subjectsSetManually = true,
            promoted = true,
            ratings = emptyList()
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata).isEqualTo(
            VideoMetadata(
                id = videoId,
                title = "video title",
                description = "video description",
                contentProvider = "content partner",
                releaseDate = LocalDate.of(2019, Month.APRIL, 19),
                keywords = listOf("k1"),
                tags = listOf("classroom"),
                durationSeconds = 10,
                source = SourceType.YOUTUBE,
                transcript = "a great transcript",
                ageRangeMin = 5,
                ageRangeMax = 11,
                type = VideoType.INSTRUCTIONAL,
                subjects = SubjectsMetadata(
                    items = setOf(SubjectMetadata(id = "subject-id", name = "subject name")),
                    setManually = true
                ),
                promoted = true,
                meanRating = null
            )
        )
    }

    @Test
    fun `tags classroom video`() {
        val video = TestFactories.createVideo(
            type = ContentType.INSTRUCTIONAL_CLIPS
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).containsExactly("classroom")
    }

    @Test
    fun `tags news video`() {
        val video = TestFactories.createVideo(
            type = ContentType.NEWS
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).contains("news")
    }

    @Test
    fun `it can apply multiple tags`() {
        val video = TestFactories.createVideo(
            description = "biology animation",
            type = ContentType.NEWS
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).containsExactly("classroom", "news")
    }

    @Test
    fun `it doesn't tag videos without a match`() {
        val video = TestFactories.createVideo(
            videoId = TestFactories.aValidId(),
            title = "garbage title",
            type = ContentType.STOCK
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).isEmpty()
    }

    @Test
    fun `converts youtube playback to youtube source type`() {
        val video = TestFactories.createVideo(
            playback = TestFactories.createYoutubePlayback(
                playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "123")
            )
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.source).isEqualTo(SourceType.YOUTUBE)
    }

    @Test
    fun `converts kaltura playback to boclips source type`() {
        val video = TestFactories.createVideo(
            playback = TestFactories.createKalturaPlayback(
                referenceId = "123"
            )
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.source).isEqualTo(SourceType.BOCLIPS)
    }

    @Test
    fun `aggregates ratings for video when converting`() {
        val ratings = listOf(3, 5, 4, 2, 4)

        val video = TestFactories.createVideo(
            ratings = ratings.map { UserRatingFactory.sample(rating = it) }
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.meanRating).isCloseTo(
            ratings.sum() / ratings.size.toDouble(),
            Offset.offset(0.00001)
        )
    }

    @Test
    fun `keeps tags explicitly set on the video`() {
        val video = TestFactories.createVideo(
            type = ContentType.INSTRUCTIONAL_CLIPS,
            tag = TestFactories.createUserTag(label = "explainer")
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).containsExactlyInAnyOrder("classroom", "explainer")
    }
}
