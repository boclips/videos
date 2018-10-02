package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class DeleteVideosTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var deleteVideos: DeleteVideos

    @Test
    fun `requesting deletion of an existing video deletes the video from MySQL`() {
        saveVideo(videoId = 123)

        deleteVideos.delete("123")

        assertThat(videoRepository.findById(123))
                .isEmpty
    }

    @Test
    fun `requesting deletion removes the video from searches`() {
        saveVideo(videoId = 123)

        deleteVideos.delete("123")

        assertThat(fakeSearchService.search("irrelevant query"))
                .doesNotContain(VideoId(videoId = "123"))
    }

    @Test
    fun `requesting deletion with blank video ID throws an exception`() {
        assertThatThrownBy { deleteVideos.delete("   ") }
                .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion with null video ID throws an exception`() {
        assertThatThrownBy { deleteVideos.delete(null) }
                .isInstanceOf(VideoNotFoundException::class.java)
    }
}