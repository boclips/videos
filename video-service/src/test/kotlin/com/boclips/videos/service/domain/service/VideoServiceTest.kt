package com.boclips.videos.service.domain.service

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.model.VideoSubjectsUpdate
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoService: VideoService

    @Test
    fun `retrieve videos by query returns Kaltura videos`() {
        saveVideo(videoId = 1, title = "a kaltura asset", playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1"))

        val videos = videoService.search(VideoSearchQuery(text = "kaltura", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 10, pageIndex = 0))

        assertThat(videos).isNotEmpty
        assertThat(videos.first().asset.title).isEqualTo("a kaltura asset")
        assertThat(videos.first().playback.thumbnailUrl).isNotBlank()
    }

    @Test
    fun `retrieve videos by query returns Youtube videos`() {
        saveVideo(videoId = 1, title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"))

        val videos = videoService.search(VideoSearchQuery(text = "youtube", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 10, pageIndex = 0))

        assertThat(videos).isNotEmpty
        assertThat(videos.first().asset.title).isEqualTo("a youtube asset")
        assertThat(videos.first().playback.thumbnailUrl).isNotBlank()
    }

    @Test
    fun `count videos`() {
        saveVideo(videoId = 1, title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"))

        val size = videoService.count(VideoSearchQuery(text = "youtube", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 10, pageIndex = 0))

        assertThat(size).isEqualTo(1)
    }

    @Test
    fun `look up video by id`() {
        saveVideo(videoId = 1)

        val video = videoService.get(AssetId("1"))

        assertThat(video).isNotNull
        assertThat(video.playback.thumbnailUrl).isEqualTo("https://thumbnail/thumbnail-entry-1.mp4")
    }

    @Test
    fun `look up video by id throws if no playback information if present`() {
        saveVideo(videoId = 123, playbackId = PlaybackId(value = "1111", type = PlaybackProviderType.KALTURA))

        fakeKalturaClient.clear()

        Assertions.assertThatThrownBy { videoService.get(AssetId("123")) }.isInstanceOf(VideoPlaybackNotFound::class.java)
    }

    @Test
    fun `look up by id throws if video does not exist`() {
        Assertions.assertThatThrownBy { videoService.get(AssetId("123")) }.isInstanceOf(VideoAssetNotFoundException::class.java)
    }

    @Test
    fun `update video asset`() {
        saveVideo(videoId = 123)

        val assetId = AssetId("123")
        val video = videoService.update(assetId, VideoSubjectsUpdate(setOf(Subject("Maths"))))

        assertThat(video.asset.subjects).containsExactly(Subject("Maths"))
        assertThat(videoService.get(assetId).asset.subjects).containsExactly(Subject("Maths"))
    }
}