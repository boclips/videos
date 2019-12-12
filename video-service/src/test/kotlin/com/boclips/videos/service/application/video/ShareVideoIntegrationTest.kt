package com.boclips.videos.service.application.video

import com.boclips.users.client.model.TeacherPlatformAttributes
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.SecurityUserFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class ShareVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: VideoRepository

    lateinit var shareVideo: ShareVideo

    @BeforeEach
    fun setup()  {
        shareVideo = ShareVideo(userServiceClient, videoRepository)
    }
    @Test
    fun `sharing a video adds the user's shareCode to the Video Document`(@Autowired videoRepository: VideoRepository) {
        val contextUser = UserFactory.sample(id = "sharer-test@boclips.com", isPermittedToShareVideo = true)
        val clientUser = SecurityUserFactory.createClientUser(id = "sharer-test@boclips.com", teacherPlatformAttributes = TeacherPlatformAttributes("abcd"))
        val video = TestFactories.createVideo()
        userServiceClient.addUser(clientUser)
        videoRepository.create(video)

        shareVideo(video.videoId.value, contextUser)

        val videoAfterShare = videoRepository.find(video.videoId)!!
        assertThat(videoAfterShare.shareCodes).contains("abcd")
    }

    @Test
    fun `user without ROLE_SHARE_VIDEOS cannot share video`() {
        val contextUser = UserFactory.sample(id = "sharer-test@boclips.com", isPermittedToShareVideo = false)
        val clientUser = SecurityUserFactory.createClientUser(id = "sharer-test@boclips.com", teacherPlatformAttributes = TeacherPlatformAttributes("abcd"))
        val video = TestFactories.createVideo()
        userServiceClient.addUser(clientUser)
        videoRepository.create(video)

        assertThrows<OperationForbiddenException> { shareVideo(video.videoId.value, contextUser) }

        val videoAfterShare = videoRepository.find(video.videoId)!!
        assertThat(videoAfterShare.shareCodes).isEmpty()
    }

}
