package com.boclips.videos.service.application.video

import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.contentpartner.AgeRangeRequest
import com.boclips.videos.service.domain.model.OpenEndedAgeRange
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UpdateVideoIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `updates to bounded age range with no max`() {
        val videoId = saveVideo(ageRangeMin = 2, ageRangeMax = 10)

        createAgeRange(AgeRangeRequest(id = "thirteen-plus", min = 13, label = "13+"))

        updateVideo(
            id = videoId.value,
            updateRequest = VideoServiceApiFactory.createUpdateVideoRequest(ageRangeMin = 13),
            user = UserFactory.sample(id = "admin@boclips.com")
        )

        val updatedVideo = videoRepository.find(videoId)!!

        assertThat(updatedVideo.ageRange).isEqualTo(OpenEndedAgeRange(min = 13, curatedManually = true))
    }
}
