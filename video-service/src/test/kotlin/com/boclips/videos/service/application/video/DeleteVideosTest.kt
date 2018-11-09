package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DeleteVideosTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var deleteVideos: DeleteVideos

    @Autowired
    lateinit var videoService: VideoService

    @Test
    fun `requesting deletion of an existing video deletes the video`() {
        saveVideo(videoId = 123)

        deleteVideos.execute("123")

        assertThatThrownBy { videoService.findVideoBy(VideoId(value = "123")) }
                .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion with blank video ID throws an exception`() {
        assertThatThrownBy { deleteVideos.execute("   ") }
                .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion with null video ID throws an exception`() {
        assertThatThrownBy { deleteVideos.execute(null) }
                .isInstanceOf(VideoNotFoundException::class.java)
    }
}
