package com.boclips.videos.service.domain.service

import com.boclips.search.service.domain.SearchService
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoService: VideoService

    @Autowired
    lateinit var searchService: SearchService

    @Autowired
    lateinit var kalturaPlaybackProvider: KalturaPlaybackProvider


    @Test
    fun `retrieve videos by query returns Kaltura videos`() {
        saveVideo(videoId = 1, title = "a kaltura video", playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1"))

        val videos = videoService.findVideosBy(VideoSearchQuery("kaltura"))

        assertThat(videos).isNotEmpty
        assertThat(videos.first().details.title).isEqualTo("a kaltura video")
        assertThat(videos.first().playback.thumbnailUrl).isNotBlank()
    }

    @Test
    fun `retrieve videos by query returns Youtube videos`() {
        saveVideo(videoId = 1, title = "a youtube video", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"))

        val videos = videoService.findVideosBy(VideoSearchQuery("youtube"))

        assertThat(videos).isNotEmpty
        assertThat(videos.first().details.title).isEqualTo("a youtube video")
        assertThat(videos.first().playback.thumbnailUrl).isNotBlank()
    }

    @Test
    fun `retrieve video by id`() {
        saveVideo(videoId = 1)

        val video = videoService.findVideoBy(VideoId("1"))

        assertThat(video).isNotNull
        assertThat(video.playback!!.thumbnailUrl).isEqualTo("https://thumbnail/thumbnail-entry-1.mp4")
    }

    @Test
    fun `retrieve video by id throws if no playback information if present`() {
        saveVideo(videoId = 123, playbackId = PlaybackId(value = "1111", type = PlaybackProviderType.KALTURA))

        fakeKalturaClient.clear()

        Assertions.assertThatThrownBy { videoService.findVideoBy(VideoId("123")) }.isInstanceOf(VideoPlaybackNotFound::class.java)
    }

    @Test
    fun `retrieve video by id throws if video does not exist`() {
        Assertions.assertThatThrownBy { videoService.findVideoBy(VideoId("123")) }.isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `remove deletes a video from repository`() {
        val videoId = VideoId(value = "123")
        saveVideo(videoId = videoId.value.toLong(), title = "Some title", description = "test description 3")

        videoService.removeVideo(videoService.findVideoBy(videoId))

        Assertions.assertThatThrownBy { videoService.findVideoBy(VideoId(value = "123")) }
                .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `remove deletes a video from search service`() {
        val videoId = VideoId(value = "123")
        saveVideo(videoId = videoId.value.toLong(), title = "Some title", description = "test description 3")

        videoService.removeVideo(videoService.findVideoBy(videoId))

        assertThat(searchService.search("Some title")).isEmpty()
    }

    @Test
    fun `remove deletes a video from Kaltura`() {
        val videoId = VideoId(value = "123")
        saveVideo(videoId = videoId.value.toLong(), title = "Some title", description = "test description 3")

        val videoToBeRemoved = videoService.findVideoBy(videoId)
        videoService.removeVideo(videoToBeRemoved)

        assertThat(kalturaPlaybackProvider.retrievePlayback(listOf(videoToBeRemoved.details.playbackId.value))).isEmpty()
    }
}