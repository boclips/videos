package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoCreated
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.EventConverter
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argWhere
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class VideoSearchUpdaterIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Nested
    inner class OnVideoCreated {
        @Test
        fun `does not add new video to search indices`() {
            val video = createVideo(distributionMethods = setOf())

            fakeEventBus.publish(
                VideoCreated.builder()
                    .video(EventConverter().toVideoPayload(video))
                    .build()
            )

            verify(legacyVideoSearchService).removeFromSearch(video.videoId.value)
            assertThat(videoSearchService.count(VideoQuery(ids = listOf(video.videoId.value)))).isEqualTo(0)
        }
    }

    @Nested
    inner class OnVideoUpdated {
        @Test
        fun `add updated video to download index`() {
            val video = createVideo(setOf(DistributionMethod.DOWNLOAD))

            fakeEventBus.publish(
                com.boclips.eventbus.events.video.VideoUpdated.builder()
                    .video(EventConverter().toVideoPayload(video))
                    .build()
            )

            verify(legacyVideoSearchService, times(1)).upsert(any(), anyOrNull())
            assertThat(videoSearchService.count(VideoQuery(ids = listOf(video.videoId.value)))).isEqualTo(0)
        }

        @Test
        fun `add updated video to stream index`() {
            val video = createVideo(distributionMethods = setOf(DistributionMethod.STREAM))

            fakeEventBus.publish(
                com.boclips.eventbus.events.video.VideoUpdated.builder()
                    .video(EventConverter().toVideoPayload(video))
                    .build()
            )

            verify(legacyVideoSearchService, times(0)).upsert(any(), anyOrNull())
            assertThat(videoSearchService.count(VideoQuery(ids = listOf(video.videoId.value)))).isEqualTo(1)
        }

        @Test
        fun `add updated video to both download and stream index`() {
            val video = createVideo(distributionMethods = setOf(DistributionMethod.STREAM, DistributionMethod.DOWNLOAD))

            fakeEventBus.publish(
                com.boclips.eventbus.events.video.VideoUpdated.builder()
                    .video(EventConverter().toVideoPayload(video))
                    .build()
            )

            verify(legacyVideoSearchService, times(1)).upsert(any(), anyOrNull())
            assertThat(videoSearchService.count(VideoQuery(ids = listOf(video.videoId.value)))).isEqualTo(1)
        }
    }

    @Nested
    inner class OnVideosUpdated {
        @Test
        fun `add stream videos to stream index`() {
            val videos = listOf(createVideo(setOf(DistributionMethod.STREAM)))

            fakeEventBus.publish(
                com.boclips.eventbus.events.video.VideosUpdated.builder()
                    .videos(videos.map { EventConverter().toVideoPayload(it) })
                    .build()
            )

            assertThat(videoSearchService.count(VideoQuery(ids = videos.map { it.videoId.value }))).isEqualTo(1)
            verify(legacyVideoSearchService, times(1)).bulkRemoveFromSearch(any())
        }

        @Test
        fun `add download videos to download index`() {
            val videos = listOf(createVideo(setOf(DistributionMethod.DOWNLOAD)))

            fakeEventBus.publish(
                com.boclips.eventbus.events.video.VideosUpdated.builder()
                    .videos(videos.map { EventConverter().toVideoPayload(it) })
                    .build()
            )

            assertThat(videoSearchService.count(VideoQuery(ids = videos.map { it.videoId.value }))).isEqualTo(0)
            verify(legacyVideoSearchService, times(1)).upsert(any(), anyOrNull())
        }

        @Test
        fun `add updated videos to correct index`() {
            val downloadVideo = createVideo(setOf(DistributionMethod.DOWNLOAD))
            val streamVideo = createVideo(setOf(DistributionMethod.STREAM))
            val unindexedVideo = createVideo(emptySet())

            val videos = listOf(
                downloadVideo,
                streamVideo,
                unindexedVideo
            )

            fakeEventBus.publish(
                com.boclips.eventbus.events.video.VideosUpdated.builder()
                    .videos(videos.map { EventConverter().toVideoPayload(it) })
                    .build()
            )

            verify(legacyVideoSearchService, times(1)).upsert(any(), anyOrNull())
            verify(legacyVideoSearchService, times(1)).bulkRemoveFromSearch(any())
            assertThat(videoSearchService.count(VideoQuery(ids = videos.map { it.videoId.value }))).isEqualTo(1)
        }

        @Test
        fun `ignores missing video in bulk update`() {
            val savedVideo = createVideo(setOf(DistributionMethod.STREAM))
            val missingVideo = TestFactories.createVideo()

            fakeEventBus.publish(
                com.boclips.eventbus.events.video.VideosUpdated.builder()
                    .videos(
                        listOf(
                            EventConverter().toVideoPayload(savedVideo),
                            EventConverter().toVideoPayload(missingVideo)
                        )
                    )
                    .build()
            )

            assertThat(videoSearchService.count(VideoQuery(ids = listOf(savedVideo.videoId.value)))).isEqualTo(1)
            assertThat(videoSearchService.count(VideoQuery(ids = listOf(missingVideo.videoId.value)))).isEqualTo(0)
        }

        @Test
        fun `never adds youtube videos to legacy search`() {
            val savedVideo = videoRepository.create(
                TestFactories.createVideo(
                    distributionMethods = setOf(DistributionMethod.DOWNLOAD),
                    playback = VideoPlayback.YoutubePlayback(
                        id = PlaybackId.from("hi", "YOUTUBE"),
                        duration = Duration.ofSeconds(1),
                        thumbnailUrl = "a-url"
                    )
                )
            )

            fakeEventBus.publish(
                com.boclips.eventbus.events.video.VideosUpdated.builder()
                    .videos(
                        listOf(
                            EventConverter().toVideoPayload(savedVideo)
                        )
                    )
                    .build()
            )

            verify(legacyVideoSearchService, times(1)).upsert(
                argWhere { it.toList().isEmpty() },
                anyOrNull()
            )
        }
    }

    @Nested
    inner class ErrorHandling {
        @Test
        fun `streaming index is still updated if removing from legacy search service fails`() {
            whenever(legacyVideoSearchService.removeFromSearch(any())).thenThrow(RuntimeException())

            val video = createVideo(distributionMethods = setOf(DistributionMethod.STREAM))

            assertThat(videoSearchService.count(VideoQuery(ids = listOf(video.videoId.value)))).isEqualTo(1)
        }

        @Test
        fun `streaming index is still updated if adding to legacy search service fails`() {
            val video = createVideo(distributionMethods = setOf(DistributionMethod.STREAM, DistributionMethod.DOWNLOAD))

            whenever(legacyVideoSearchService.upsert(any(), any())).thenThrow(RuntimeException())

            assertThat(videoSearchService.count(VideoQuery(ids = listOf(video.videoId.value)))).isEqualTo(1)
        }
    }

    private fun createVideo(distributionMethods: Set<DistributionMethod>): Video {
        val video = videoRepository.create(
            TestFactories.createVideo(
                distributionMethods = distributionMethods
            )
        )
        reset(legacyVideoSearchService)
        return video
    }
}
