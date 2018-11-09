package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MysqlVideoLibraryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: MysqlVideoLibrary

    @Test
    fun `findVideosBy can find multiple videos by video ids`() {
        saveVideo(videoId = 123, title = "Some title", description = "test description 3")
        saveVideo(videoId = 124, title = "Some title", description = "test description 3")
        saveVideo(videoId = 125, title = "Some title", description = "test description 3")

        val videos = videoRepository.findVideosBy(listOf(VideoId(value = "123"), VideoId(value = "124"), VideoId(value = "125")))

        assertThat(videos).hasSize(3)
    }

    @Test
    fun `findVideosBy does not throw when one video can't be found`() {
        saveVideo(videoId = 123, title = "Some title", description = "test description 3")
        saveVideo(videoId = 124, title = "Some title", description = "test description 3")

        val videos = videoRepository.findVideosBy(listOf(VideoId(value = "123"), VideoId(value = "124"), VideoId(value = "125")))

        assertThat(videos).hasSize(2)
    }

    @Test
    fun `findVideoBy returns video details`() {
        saveVideo(videoId = 123, title = "Some title", description = "test description 3")

        val videoId = VideoId(value = "123")
        val video = videoRepository.findVideoBy(videoId)!!

        assertThat(video.videoId.value).isEqualTo("123")
        assertThat(video.playbackId.value).isNotNull()
        assertThat(video.playbackId.type).isNotNull()
        assertThat(video.description).isNotEmpty()
        assertThat(video.title).isNotEmpty()
        assertThat(video.contentProvider).isNotEmpty()
        assertThat(video.releasedOn).isNotNull()
    }

    @Test
    fun `findVideoBy returns null when video does not exist`() {
        assertThat(videoRepository.findVideoBy(VideoId(value = "999"))).isNull()
    }

    @Test
    fun `video cannot be retrieved after it has been removed`() {
        val videoId = VideoId("123")
        saveVideo(videoId = videoId.value.toLong(), title = "Some title", description = "test description 3")

        videoRepository.deleteVideoBy(videoId)

        assertThat(videoRepository.findVideosBy(listOf(videoId))).isEmpty()
    }
}
