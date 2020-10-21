package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.request.VideoRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoRetrievalServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRetrievalService: VideoRetrievalService

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Nested
    inner class Retrieving {
        @Test
        fun `retrieve videos by query returns Kaltura videos`() {
            saveVideo(
                playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1"),
                title = "a kaltura video"
            )

            val results = videoRetrievalService.searchPlayableVideos(
                VideoRequest(
                    text = "kaltura",
                    pageSize = 10,
                    pageIndex = 0
                ),
                VideoAccess.Everything
            )

            assertThat(results.videos).isNotEmpty
            assertThat(results.videos.first().title).isEqualTo("a kaltura video")
        }

        @Test
        fun `retrieve videos by query returns Youtube videos`() {
            saveVideo(
                playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"),
                title = "a youtube video"
            )

            val results = videoRetrievalService.searchPlayableVideos(
                VideoRequest(
                    text = "youtube",
                    pageSize = 10,
                    pageIndex = 0
                ),
                VideoAccess.Everything
            )

            assertThat(results.videos).isNotEmpty
            assertThat(results.videos.first().title).isEqualTo("a youtube video")
            assertThat((results.videos.first().playback as VideoPlayback.YoutubePlayback).thumbnailUrl).isNotBlank()
        }

        @Test
        fun `look up video by id`() {
            val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "abc"))

            val video = videoRetrievalService.getPlayableVideo(videoId, VideoAccess.Everything)

            assertThat(video).isNotNull
        }

        @Test
        fun `look up videos by ids`() {
            val videoId1 = saveVideo()
            saveVideo()
            val videoId2 = saveVideo()

            val video = videoRetrievalService.getPlayableVideos(listOf(videoId1, videoId2), VideoAccess.Everything)

            assertThat(video).hasSize(2)
            assertThat(video.map { it.videoId }).containsExactly(videoId1, videoId2)
        }

        @Test
        fun `look up videos honors order`() {
            val videoId1 = saveVideo()
            val videoId2 = saveVideo()
            val videoId3 = saveVideo()

            val videos =
                videoRetrievalService.getPlayableVideos(listOf(videoId3, videoId1, videoId2), VideoAccess.Everything)
            assertThat(videos.map { it.videoId }).containsExactly(videoId3, videoId1, videoId2)
        }

        @Test
        fun `look up by id throws if video does not exist`() {
            Assertions.assertThatThrownBy {
                videoRetrievalService.getPlayableVideo(
                    VideoId(value = TestFactories.aValidId()),
                    VideoAccess.Everything
                )
            }
                .isInstanceOf(VideoNotFoundException::class.java)
        }

        @Test
        fun `retrieve all video ids with Everything permission`() {
            val videoId1 = saveVideo(title = "video 1")
            val videoId2 = saveVideo(title = "video 2")
            val videoId3 = saveVideo(title = "video 3")

            val videoIds = videoRetrievalService.getVideoIds(
                pageIndex = 0,
                pageSize = 5,
                videoAccess = VideoAccess.Everything
            )

            assertThat(videoIds).containsExactlyInAnyOrder(videoId1, videoId2, videoId3)
        }

        @Test
        fun `respect page and size parameters in video ids query`() {
            saveVideo(title = "video 1")
            saveVideo(title = "video 2")
            saveVideo(title = "video 3")

            val videoIds = videoRetrievalService.getVideoIds(
                pageIndex = 1,
                pageSize = 2,
                videoAccess = VideoAccess.Everything
            )
            assertThat(videoIds).hasSize(1)
        }

        @Test
        fun `restrict to video access rules in video ids query`() {
            val video1 = saveVideo(title = "1")
            saveVideo(title = "2")
            val video3 = saveVideo(title = "3")

            val videoIds = videoRetrievalService.getVideoIds(
                pageIndex = 0,
                pageSize = 5,
                videoAccess = VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.IncludedIds(
                            setOf(video1, video3)
                        )
                    )
                )
            )

            assertThat(videoIds).containsExactlyInAnyOrder(
                video1, video3
            )
        }
    }
}
