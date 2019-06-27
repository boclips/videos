package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.subjects.SubjectId
import com.boclips.videos.service.domain.model.subjects.Subject
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
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
            type = LegacyVideoType.INSTRUCTIONAL_CLIPS,
            keywords = listOf("k1"),
            releasedOn = LocalDate.of(2019, Month.APRIL, 19),
            transcript = "a great transcript",
            ageRange = AgeRange.bounded(5, 11),
            subjects = setOf(
                Subject(
                    id = SubjectId(value = "1"),
                    name = "fancy"
                )
            )
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
                subjects = setOf("1")
            )
        )
    }

    @Test
    fun `tags classroom video`() {
        val video = TestFactories.createVideo(
            type = LegacyVideoType.INSTRUCTIONAL_CLIPS
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).containsExactly("classroom")
    }

    @Test
    fun `tags news video`() {
        val video = TestFactories.createVideo(
            type = LegacyVideoType.NEWS
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).contains("news")
    }

    @Test
    fun `it can apply multiple tags`() {
        val video = TestFactories.createVideo(
            description = "biology animation",
            type = LegacyVideoType.NEWS
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.tags).containsExactly("classroom", "news")
    }

    @Test
    fun `it doesn't tag videos without a match`() {
        val video = TestFactories.createVideo(
            videoId = TestFactories.aValidId(),
            title = "garbage title",
            type = LegacyVideoType.STOCK
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
                playbackId = "123"
            )
        )

        val videoMetadata = VideoMetadataConverter.convert(video)

        assertThat(videoMetadata.source).isEqualTo(SourceType.BOCLIPS)
    }
}
