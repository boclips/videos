package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoSearchQuery
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoService: VideoService

    @Autowired
    lateinit var contentPartnerRepository: ContentPartnerRepository

    @Test
    fun `retrieve videos by query returns Kaltura videos`() {
        saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1"),
            title = "a kaltura video"
        )

        val videos = videoService.search(
            VideoSearchQuery(
                text = "kaltura",
                includeTags = emptyList(),
                excludeTags = emptyList(),
                pageSize = 10,
                pageIndex = 0
            )
        )

        assertThat(videos).isNotEmpty
        assertThat(videos.first().title).isEqualTo("a kaltura video")
        assertThat(videos.first().playback.thumbnailUrl).isNotBlank()
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
                includeTags = emptyList(),
                excludeTags = emptyList(),
                pageSize = 10,
                pageIndex = 0
            )
        )

        assertThat(videos).isNotEmpty
        assertThat(videos.first().title).isEqualTo("a youtube video")
        assertThat(videos.first().playback.thumbnailUrl).isNotBlank()
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
                includeTags = emptyList(),
                excludeTags = emptyList(),
                pageSize = 10,
                pageIndex = 0
            )
        )

        assertThat(size).isEqualTo(1)
    }

    @Test
    fun `look up video by id`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "abc"))

        val video = videoService.getPlayableVideo(videoId)

        assertThat(video).isNotNull
        assertThat(video.playback.thumbnailUrl).isEqualTo("https://thumbnail/thumbnail-entry-abc.mp4")
    }

    @Test
    fun `look up videos by ids`() {
        val videoId1 = saveVideo()
        saveVideo()
        val videoId2 = saveVideo()

        val video = videoService.getPlayableVideo(listOf(videoId1, videoId2))

        assertThat(video).hasSize(2)
        assertThat(video.map { it.videoId }).containsExactly(videoId1, videoId2)
    }

    @Test
    fun `look up by id throws if video does not exist`() {
        Assertions.assertThatThrownBy { videoService.getPlayableVideo(VideoId(value = TestFactories.aValidId())) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `create video with an age range`() {
        val ageRange = AgeRange.bounded(2, 5)
        val video = videoService.create(TestFactories.createVideo(ageRange = ageRange))

        assertThat(videoService.getPlayableVideo(video.videoId).ageRange).isEqualTo(ageRange)
    }

    @Test
    fun `create video with no age range`() {
        contentPartnerRepository.create(
            contentPartner = TestFactories.createContentPartner(
                name = "Our content partner",
                ageRange = AgeRange.bounded(3, 7)
            )
        )

        val video = videoService.create(
            TestFactories.createVideo(
                contentPartnerName = "Our content partner",
                ageRange = AgeRange.unbounded()
            )
        )

        assertThat(video.ageRange.min()).isEqualTo(3)
        assertThat(video.ageRange.max()).isEqualTo(7)
    }
}
