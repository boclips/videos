package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MysqlVideoRepositoryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: MysqlVideoRepository

    @Test
    fun `findVideosBy can find multiple videos by video ids`() {
        saveVideo(videoId = 123, title = "Some title", description = "test description 3")
        saveVideo(videoId = 124, title = "Some title", description = "test description 3")
        saveVideo(videoId = 125, title = "Some title", description = "test description 3")

        val videos = videoRepository.findVideosBy(listOf(VideoId(videoId = "123"), VideoId(videoId = "124"), VideoId(videoId = "125")))

        assertThat(videos).hasSize(3)
    }

    @Test
    fun `findVideosBy does not throw when one video can't be found`() {
        saveVideo(videoId = 123, title = "Some title", description = "test description 3")
        saveVideo(videoId = 124, title = "Some title", description = "test description 3")

        val videos = videoRepository.findVideosBy(listOf(VideoId(videoId = "123"), VideoId(videoId = "124"), VideoId(videoId = "125")))

        assertThat(videos).hasSize(2)
    }

    @Test
    fun `findVideoBy returns a Video without playback information`() {
        saveVideo(videoId = 123, title = "Some title", description = "test description 3")

        val videoId = VideoId(videoId = "123")
        val video = videoRepository.findVideoBy(videoId)!!

        assertThat(video.videoId.videoId).isEqualTo("123")
        assertThat(video.playbackId.playbackId).isNotNull()
        assertThat(video.playbackId.playbackProviderType).isNotNull()
        assertThat(video.videoPlayback).isNull()
        assertThat(video.description).isNotEmpty()
        assertThat(video.title).isNotEmpty()
        assertThat(video.contentProvider).isNotEmpty()
        assertThat(video.releasedOn).isNotNull()

        assertThat(video.isPlayable()).isFalse()
    }

    @Test
    fun `findVideoBy returns null when video does not exist`() {
        assertThat(videoRepository.findVideoBy(VideoId(videoId = "999"))).isNull()
    }

    @Test
    fun `video cannot be retrieved after it has been removed`() {
        val videoId = VideoId("123")
        saveVideo(videoId = videoId.videoId.toLong(), title = "Some title", description = "test description 3")

        videoRepository.deleteVideoById(videoId)

        assertThat(videoRepository.findVideosBy(listOf(videoId))).isEmpty()
    }
}
