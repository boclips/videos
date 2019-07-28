package com.boclips.videos.service.domain.service.video

import com.boclips.eventbus.events.video.VideoSubjectClassificationRequested
import com.boclips.eventbus.events.video.VideosInclusionInDownloadRequested
import com.boclips.eventbus.events.video.VideosInclusionInStreamRequested
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.video.VideoSearchQuery
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import io.micrometer.core.instrument.Counter
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
            title = "a kaltura video",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")
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
            title = "a youtube video",
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123")
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
            title = "a youtube video",
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123")
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

    @Test
    fun `created video becomes available in search`() {
        videoService.create(
            videoToBeCreated = TestFactories.createVideo(
                distributionMethods = DistributionMethod.ALL
            )
        )

        assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInStreamRequested::class.java)).isTrue()
        assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInDownloadRequested::class.java)).isTrue()
    }

    @Test
    fun `created video becomes available in stream search only`() {
        videoService.create(
            videoToBeCreated =
            TestFactories.createVideo(distributionMethods = setOf(DistributionMethod.STREAM))
        )

        assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInStreamRequested::class.java)).isTrue()
        assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInDownloadRequested::class.java)).isFalse()
    }

    @Test
    fun `created video becomes available in download search only`() {
        videoService.create(
            videoToBeCreated = TestFactories.createVideo(distributionMethods = setOf(DistributionMethod.DOWNLOAD))
        )

        assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInStreamRequested::class.java)).isFalse()
        assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInDownloadRequested::class.java)).isTrue()
    }

    @Test
    fun `created video does not become available in search`() {
        videoService.create(videoToBeCreated = TestFactories.createVideo(distributionMethods = emptySet()))

        assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInStreamRequested::class.java)).isFalse()
        assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInDownloadRequested::class.java)).isFalse()
    }

    @Test
    fun `does not populate legacy search when youtube video is created`() {
        videoService.create(
            videoToBeCreated = TestFactories.createVideo(
                playback = TestFactories.createYoutubePlayback()
            )
        )

        assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInDownloadRequested::class.java)).isFalse()
    }
}
