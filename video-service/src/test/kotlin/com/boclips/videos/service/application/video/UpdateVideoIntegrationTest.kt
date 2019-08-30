package com.boclips.videos.service.application.video

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UpdateVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var updateVideo: UpdateVideo

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `only specified fields are updated`() {
        val videoId = saveVideo(title = "title", description = "description")

        setSecurityContext("admin@boclips.com", UserRoles.UPDATE_VIDEOS)

        updateVideo(
            id = videoId.value,
            title = null,
            description = "new description"
        )

        val updatedVideo = videoRepository.find(videoId)!!

        assertThat(updatedVideo.title).isEqualTo("title")
        assertThat(updatedVideo.description).isEqualTo("new description")
    }
}
