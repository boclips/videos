package com.boclips.videos.service.application.video.indexing

import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.eventbus.events.video.VideoCreated
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.service.events.EventConverter
import com.boclips.videos.service.domain.service.video.VideoRepository
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

class VideoIndexUpdaterIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var channelRepository: ChannelRepository

    @Nested
    inner class OnVideoCreated {
        @Test
        fun `add new video to search indices`() {
            val video = createVideo(emptySet())

            fakeEventBus.publish(
                VideoCreated.builder()
                    .video(EventConverter().toVideoPayload(video))
                    .build()
            )

            verify(legacyVideoSearchService).removeFromSearch(video.videoId.value)

            assertThat(
                videoIndexFake.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            userQuery = UserQuery(ids = setOf(video.videoId.value)),
                            videoAccessRuleQuery = VideoAccessRuleQuery()
                        )
                    )
                ).counts.totalHits
            ).isEqualTo(1)
        }
    }

    @Nested
    inner class OnVideoUpdated {
        @Test
        fun `add updated video to both indices`() {
            val video = createVideo(distributionMethods = setOf(DistributionMethod.DOWNLOAD))

            fakeEventBus.publish(
                com.boclips.eventbus.events.video.VideoUpdated.builder()
                    .video(EventConverter().toVideoPayload(video))
                    .build()
            )

            verify(legacyVideoSearchService, times(1)).upsert(any(), anyOrNull())
            assertThat(
                videoIndexFake.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            userQuery = UserQuery(ids = setOf(video.videoId.value)),
                            videoAccessRuleQuery = VideoAccessRuleQuery()
                        )
                    )
                ).counts.totalHits
            ).isEqualTo(1)
        }
    }

    @Nested
    inner class OnVideosUpdated {
        @Test
        fun `add stream videos to non-legacy index only`() {
            val videos = listOf(
                createVideo(setOf(DistributionMethod.STREAM)), createVideo(
                    setOf(
                        DistributionMethod.DOWNLOAD
                    )
                )
            )

            fakeEventBus.publish(
                com.boclips.eventbus.events.video.VideosUpdated.builder()
                    .videos(videos.map {
                        EventConverter()
                            .toVideoPayload(it)
                    })
                    .build()
            )

            assertThat(
                videoIndexFake.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            userQuery = UserQuery(
                                ids = videos.map { it.videoId.value }.toSet()
                            ),
                            videoAccessRuleQuery = VideoAccessRuleQuery()
                        )
                    )
                ).counts.totalHits
            ).isEqualTo(2)

            verify(legacyVideoSearchService, times(1)).bulkRemoveFromSearch(any())
        }

        @Test
        fun `add download videos to both indices`() {
            val videos = listOf(createVideo(setOf(DistributionMethod.DOWNLOAD)))

            fakeEventBus.publish(
                com.boclips.eventbus.events.video.VideosUpdated.builder()
                    .videos(videos.map {
                        EventConverter()
                            .toVideoPayload(it)
                    })
                    .build()
            )

            assertThat(
                videoIndexFake.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            userQuery = UserQuery(
                                ids = videos.map { it.videoId.value }.toSet()
                            ),
                            videoAccessRuleQuery = VideoAccessRuleQuery()
                        )
                    )
                ).counts.totalHits
            ).isEqualTo(1)

            verify(
                legacyVideoSearchService, times(1)
            ).upsert(any(), anyOrNull())
        }

        @Test
        fun `add updated videos to correct index`() {
            val downloadVideo = createVideo(setOf(DistributionMethod.DOWNLOAD))
            val streamVideo = createVideo(setOf(DistributionMethod.STREAM))
            val video = createVideo(emptySet())

            val videos = listOf(
                downloadVideo,
                streamVideo,
                video
            )

            fakeEventBus.publish(
                com.boclips.eventbus.events.video.VideosUpdated.builder()
                    .videos(videos.map {
                        EventConverter()
                            .toVideoPayload(it)
                    })
                    .build()
            )

            verify(legacyVideoSearchService, times(1)).upsert(any(), anyOrNull())
            verify(legacyVideoSearchService, times(1)).bulkRemoveFromSearch(any())
            assertThat(
                videoIndexFake.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            userQuery = UserQuery(
                                ids = videos.map { it.videoId.value }.toSet()
                            ),
                            videoAccessRuleQuery = VideoAccessRuleQuery()
                        )
                    )
                ).counts.totalHits
            ).isEqualTo(3)
        }

        @Test
        fun `never adds youtube videos to legacy search`() {
            val savedVideo = videoRepository.create(
                TestFactories.createVideo(
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

            val video = createVideo(setOf(DistributionMethod.DOWNLOAD, DistributionMethod.STREAM))

            assertThat(
                videoIndexFake.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            userQuery = UserQuery(ids = setOf(video.videoId.value)),
                            videoAccessRuleQuery = VideoAccessRuleQuery()
                        )
                    )
                ).counts.totalHits
            ).isEqualTo(1)
        }

        @Test
        fun `streaming index is still updated if adding to legacy search service fails`() {
            val video = createVideo(setOf(DistributionMethod.DOWNLOAD, DistributionMethod.STREAM))

            whenever(legacyVideoSearchService.upsert(any(), any())).thenThrow(RuntimeException())

            assertThat(
                videoIndexFake.search(
                    PaginatedIndexSearchRequest(
                        query = VideoQuery(
                            userQuery = UserQuery(ids = setOf(video.videoId.value)),
                            videoAccessRuleQuery = VideoAccessRuleQuery()
                        )
                    )
                ).counts.totalHits
            ).isEqualTo(1)
        }
    }

    private fun createVideo(distributionMethods: Set<DistributionMethod> = emptySet()): Video {
        val contentPartner = com.boclips.contentpartner.service.testsupport.ChannelFactory.createChannel(
            distributionMethods = distributionMethods
        )

        val createdContentPartner = channelRepository.create(contentPartner)

        val video = videoRepository.create(
            TestFactories.createVideo(
                channelId = ChannelId(
                    value = createdContentPartner.id.value
                )
            )
        )

        reset(legacyVideoSearchService)

        return video
    }
}
