package com.boclips.videos.service.domain.service

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoService: VideoService

    @Test
    fun `retrieve videos by query returns Kaltura videos`() {
        val videoId = saveVideo(title = "a kaltura asset", playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1"))

        val videos = videoService.search(VideoSearchQuery(text = "kaltura", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 10, pageIndex = 0))

        assertThat(videos).isNotEmpty
        assertThat(videos.first().asset.title).isEqualTo("a kaltura asset")
        assertThat(videos.first().playback.thumbnailUrl).isNotBlank()
    }

    @Test
    fun `retrieve videos by query returns Youtube videos`() {
        val videoId = saveVideo(title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"))

        val videos = videoService.search(VideoSearchQuery(text = "youtube", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 10, pageIndex = 0))

        assertThat(videos).isNotEmpty
        assertThat(videos.first().asset.title).isEqualTo("a youtube asset")
        assertThat(videos.first().playback.thumbnailUrl).isNotBlank()
    }

    @Test
    fun `count videos`() {
        saveVideo(title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"))

        val size = videoService.count(VideoSearchQuery(text = "youtube", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 10, pageIndex = 0))

        assertThat(size).isEqualTo(1)
    }

    @Test
    fun `look up video by id`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "abc"))

        val video = videoService.get(videoId)

        assertThat(video).isNotNull
        assertThat(video.playback.thumbnailUrl).isEqualTo("https://thumbnail/thumbnail-entry-abc.mp4")
    }

    @Test
    fun `look up videos by ids`() {
        val videoId1 = saveVideo()
        saveVideo()
        val videoId2 = saveVideo()

        val video = videoService.get(listOf(videoId1, videoId2))

        assertThat(video).hasSize(2)
        assertThat(video.map { it.asset.assetId }).containsExactly(videoId1, videoId2)
    }

    @Test
    fun `look up video by id throws if no playback information if present`() {
        val videoId = saveVideo(playbackId = PlaybackId(value = "1111", type = PlaybackProviderType.KALTURA))

        fakeKalturaClient.clear()

        Assertions.assertThatThrownBy { videoService.get(videoId) }.isInstanceOf(VideoPlaybackNotFound::class.java)
    }

    @Test
    fun `look up by id throws if video does not exist`() {
        Assertions.assertThatThrownBy { videoService.get(AssetId(value = TestFactories.aValidId())) }.isInstanceOf(VideoAssetNotFoundException::class.java)
    }

    @Test
    fun `update video asset`() {
        val videoId = saveVideo()

        val video = videoService.update(videoId, VideoSubjectsUpdate(setOf(Subject("Maths"))))

        assertThat(video.asset.subjects).containsExactly(Subject("Maths"))
        assertThat(videoService.get(videoId).asset.subjects).containsExactly(Subject("Maths"))
    }
}