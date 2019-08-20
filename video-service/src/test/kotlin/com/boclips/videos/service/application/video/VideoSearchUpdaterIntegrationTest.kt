package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoCreated
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.EventConverter
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

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
