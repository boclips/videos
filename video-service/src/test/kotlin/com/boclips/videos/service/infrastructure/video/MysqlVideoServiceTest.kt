package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.application.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.search.service.domain.SearchService
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class MysqlVideoServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoService: VideoService

    @Autowired
    lateinit var searchService: SearchService

    @Test
    fun `find multiple videos by video ids`() {
        saveVideo(123, "Some title", "test description 3")
        saveVideo(124, "Some title", "test description 3")
        saveVideo(125, "Some title", "test description 3")

        val videos = videoService.findVideosBy(listOf(VideoId(videoId = "123"), VideoId(videoId = "124"), VideoId(videoId = "125")))

        assertThat(videos).hasSize(3)
    }

    @Test
    fun `find multiple videos and does not throw when one video can't be found`() {
        saveVideo(123, "Some title", "test description 3")
        saveVideo(124, "Some title", "test description 3")

        val videos = videoService.findVideosBy(listOf(VideoId(videoId = "123"), VideoId(videoId = "124"), VideoId(videoId = "125")))

        assertThat(videos).hasSize(2)
    }

    @Test
    fun `find single video by video id`() {
        saveVideo(123, "Some title", "test description 3")

        val videoId = VideoId(videoId = "123")
        val video = videoService.findVideoBy(videoId)

        assertThat(video.videoId).isEqualTo(videoId)
        assertThat(video.isPlayable()).isFalse()
    }

    @Test
    fun `returns a Video without playback information`() {
        saveVideo(123, "Some title", "test description 3")

        val videoId = VideoId(videoId = "123")
        val video = videoService.findVideoBy(videoId)

        assertThat(video.videoId.videoId).isEqualTo("123")
        assertThat(video.videoId.referenceId).isNotNull()
        assertThat(video.videoPlayback).isNull()
        assertThat(video.description).isNotEmpty()
        assertThat(video.title).isNotEmpty()
        assertThat(video.contentProvider).isNotEmpty()
        assertThat(video.releasedOn).isNotNull()

        assertThat(video.isPlayable()).isFalse()
    }

    @Test
    fun `throw when video does not exist`() {
        assertThatThrownBy {
            videoService.findVideoBy(VideoId(videoId = "999"))
        }.isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `find videos with search query`() {
        saveVideo(123, "Some title", "test description 3")

        val videos = videoService.findVideosBy(VideoSearchQuery(text = "test"))

        assertThat(videos).hasSize(1)
        assertThat(videos[0].videoId.videoId == "123")
    }

    @Test
    fun `find no videos because there are none`() {
        val videos = videoService.findVideosBy(VideoSearchQuery(text = "this is a query not returning any search results"))

        assertThat(videos).hasSize(0)
    }

    @Test
    fun `remove a video`() {
        saveVideo(123, "Some title", "test description 3")

        val videoIdToBeDeleted = VideoId(videoId = "123")
        val videoToBeDeleted = videoService.findVideoBy(videoIdToBeDeleted)
        videoService.removeVideo(videoToBeDeleted)

        assertThatThrownBy { videoService.findVideoBy(VideoId(videoId = "123")) }
                .isInstanceOf(VideoNotFoundException::class.java)

        assertThat(searchService.search("Some title")).isEmpty()
    }

}
