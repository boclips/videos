package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
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
        val subjectsList = listOf(
            saveSubject(name = "Design"),
            saveSubject(name = "Art")
        )

        val subjectIdList = subjectsList.map { it.id.value }

        updateVideo(
            id = videoId.value,
            title = null,
            description = "new description",
            promoted = true,
            subjectIds = subjectIdList,
            user = UserFactory.sample(id = "admin@boclips.com")
        )

        val updatedVideo = videoRepository.find(videoId)!!

        assertThat(updatedVideo.title).isEqualTo("title")
        assertThat(updatedVideo.description).isEqualTo("new description")
        assertThat(updatedVideo.promoted).isEqualTo(true)
        assertThat(updatedVideo.subjects).containsExactlyInAnyOrder(*subjectsList.toTypedArray())
    }
}
