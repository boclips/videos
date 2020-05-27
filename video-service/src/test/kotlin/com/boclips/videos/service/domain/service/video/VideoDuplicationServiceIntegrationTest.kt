package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class VideoDuplicationServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoDuplicationService: VideoDuplicationService

    @Autowired
    lateinit var mongoVideoRepository: VideoRepository

    @Test
    fun `marks duplicate video as deactivated, save reference to new video`() {
        val oldVideo = mongoVideoRepository.create(TestFactories.createVideo(title = "The same video", deactivated = false, activeVideoId = null))
        val newVideo = mongoVideoRepository.create(TestFactories.createVideo(title = "The same video", deactivated = false, activeVideoId = null))

        videoDuplicationService.markDuplicate(oldVideo, newVideo)

        val oldVideoAfter = mongoVideoRepository.find(oldVideo.videoId)
        val newVideoAfter = mongoVideoRepository.find(newVideo.videoId)

        assertThat(oldVideoAfter!!.deactivated).isTrue()
        assertThat(oldVideoAfter.activeVideoId).isEqualTo(newVideo.videoId)

        assertThat(newVideoAfter).isEqualTo(newVideo)
    }
}