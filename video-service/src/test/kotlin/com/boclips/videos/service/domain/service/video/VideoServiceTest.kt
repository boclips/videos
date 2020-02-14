package com.boclips.videos.service.domain.service.video

import com.boclips.videos.api.request.contentpartner.AgeRangeRequest
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.ContentPartnerId
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.model.video.VideoSearchQuery
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class VideoServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoService: VideoService

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

            val videos = videoService.search(
                VideoSearchQuery(
                    text = "kaltura",
                    pageSize = 10,
                    pageIndex = 0
                ),
                VideoAccessRule.Everything
            )

            assertThat(videos).isNotEmpty
            assertThat(videos.first().title).isEqualTo("a kaltura video")
        }

        @Test
        fun `retrieve videos by query returns Youtube videos`() {
            saveVideo(
                playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"),
                title = "a youtube video"
            )

            val videos = videoService.search(
                VideoSearchQuery(
                    text = "youtube",
                    pageSize = 10,
                    pageIndex = 0
                ),
                VideoAccessRule.Everything
            )

            assertThat(videos).isNotEmpty
            assertThat(videos.first().title).isEqualTo("a youtube video")
            assertThat((videos.first().playback as VideoPlayback.YoutubePlayback).thumbnailUrl).isNotBlank()
        }

        @Test
        fun `count videos`() {
            saveVideo(
                playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"),
                title = "a youtube video"
            )

            val size = videoService.count(
                VideoSearchQuery(
                    text = "youtube",
                    pageSize = 10,
                    pageIndex = 0
                ),
                VideoAccessRule.Everything
            )

            assertThat(size).isEqualTo(1)
        }

        @Test
        fun `limits search results when specific id access rule is provided`() {
            val firstVideo = saveVideo(title = "access")
            saveVideo(title = "no access")

            val searchResults = videoService.search(
                VideoSearchQuery(
                    text = "access",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccessRule.SpecificIds(setOf(firstVideo))
            )

            assertThat(searchResults).hasSize(1)
            assertThat(searchResults.map { it.videoId }).containsExactly(firstVideo)
        }

        @Test
        fun `count takes specific ids access into ac-count (pun intended)`() {
            val firstVideo = saveVideo(title = "access")
            saveVideo(title = "no access")

            val searchResults = videoService.count(
                VideoSearchQuery(
                    text = "access",
                    pageSize = 10,
                    pageIndex = 0
                ), VideoAccessRule.SpecificIds(setOf(firstVideo))
            )

            assertThat(searchResults).isEqualTo(1)
        }

        @Test
        fun `look up video by id`() {
            val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "abc"))

            val video = videoService.getPlayableVideo(videoId, VideoAccessRule.Everything)

            assertThat(video).isNotNull
        }

        @Test
        fun `look up videos by ids`() {
            val videoId1 = saveVideo()
            saveVideo()
            val videoId2 = saveVideo()

            val video = videoService.getPlayableVideos(listOf(videoId1, videoId2), VideoAccessRule.Everything)

            assertThat(video).hasSize(2)
            assertThat(video.map { it.videoId }).containsExactly(videoId1, videoId2)
        }

        @Test
        fun `look up by id throws if video does not exist`() {
            Assertions.assertThatThrownBy {
                videoService.getPlayableVideo(
                    VideoId(value = TestFactories.aValidId()),
                    VideoAccessRule.Everything
                )
            }
                .isInstanceOf(VideoNotFoundException::class.java)
        }
    }

    @Nested
    inner class CreateVideo {
        @Test
        fun `create video with an age range`() {
            val ageRange = AgeRange.bounded(2, 5)
            val video = videoService.create(TestFactories.createVideo(ageRange = ageRange))

            assertThat(videoService.getPlayableVideo(video.videoId, VideoAccessRule.Everything).ageRange).isEqualTo(
                ageRange
            )
        }

        @Test
        fun `create video with no age range`() {
            createAgeRange(AgeRangeRequest(id = "early-years", min = 3, max = 7, label = "3-7"))
            val contentPartner = saveContentPartner(
                name = "Our content partner",
                ageRanges = listOf("early-years")
            )

            val video = videoService.create(
                TestFactories.createVideo(
                    contentPartnerName = "Our content partner",
                    contentPartnerId = ContentPartnerId(value = contentPartner.contentPartnerId.value),
                    ageRange = AgeRange.unbounded()
                )
            )

            assertThat(video.ageRange.min()).isEqualTo(3)
            assertThat(video.ageRange.max()).isEqualTo(7)
        }

        @Test
        fun `do not create video when duplicate`() {
            val contentPartner = saveContentPartner(name = "Our content partner")

            videoService.create(
                TestFactories.createVideo(
                    contentPartnerId = ContentPartnerId(value = contentPartner.contentPartnerId.value),
                    videoReference = "video-123"
                )
            )

            assertThrows<VideoNotCreatedException> {
                videoService.create(
                    TestFactories.createVideo(
                        contentPartnerId = ContentPartnerId(value = contentPartner.contentPartnerId.value),
                        videoReference = "video-123"
                    )
                )
            }
        }

        @Test
        fun `create video with best for tags`() {
            val tagLabel = "explainer"
            val video = videoService.create(
                TestFactories.createVideo(
                    tags = listOf(TestFactories.createUserTag(label = tagLabel))
                )
            )

            assertThat(video.tags.first().tag.label).isEqualTo(tagLabel)
        }
    }
}
