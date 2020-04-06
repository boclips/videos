package com.boclips.videos.service.application.video

import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.contentpartner.AgeRangeRequest
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.LowerBoundedAgeRange
import com.boclips.videos.service.domain.model.SpecificAgeRange
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
    fun `matching fields are updated, subjects and subjectsWereSetManually included`() {
        val videoId = saveVideo(title = "title", description = "description")
        val subjectsList = listOf(
            saveSubject(name = "Design"),
            saveSubject(name = "Art")
        )
        val subjectIdList = subjectsList.map { it.id.value }

        updateVideo(
            id = videoId.value,
            updateRequest = VideoServiceApiFactory.createUpdateVideoRequest(
                title = null,
                description = "new description",
                promoted = true,
                subjectIds = subjectIdList,
                ageRangeMin = 3,
                ageRangeMax = 7
            ),
            user = UserFactory.sample(id = "admin@boclips.com")
        )

        val updatedVideo = videoRepository.find(videoId)!!

        assertThat(updatedVideo.title).isEqualTo("title")
        assertThat(updatedVideo.description).isEqualTo("new description")
        assertThat(updatedVideo.promoted).isEqualTo(true)
        assertThat(updatedVideo.subjects.items).containsExactlyInAnyOrder(*subjectsList.toTypedArray())
        assertThat(updatedVideo.subjects.setManually).isTrue()
        assertThat(updatedVideo.ageRange).isEqualTo(SpecificAgeRange(3, 7))
    }

    @Test
    fun `with no subjects specified, subjectsWereSetManually stays false`() {
        val videoId = saveVideo(
            title = "title",
            description = "description",
            subjectIds = emptySet()
        )
        updateVideo(
            id = videoId.value,
            updateRequest = VideoServiceApiFactory.createUpdateVideoRequest(
                title = null,
                description = "new description",
                promoted = true,
                subjectIds = null
            ),
            user = UserFactory.sample(id = "admin@boclips.com")
        )

        val updatedVideo = videoRepository.find(videoId)!!

        assertThat(updatedVideo.subjects.items).isEmpty()
        assertThat(updatedVideo.subjects.setManually).isFalse()
    }

    @Test
    fun `updates to bounded agerange with no max`() {
        val videoId = saveVideo(ageRange = AgeRange.of(min = 2, max = 10))

        createAgeRange(AgeRangeRequest(id = "thirteen-plus", min = 13, label = "13+"))

        updateVideo(
            id = videoId.value,
            updateRequest = VideoServiceApiFactory.createUpdateVideoRequest(
                ageRangeMin = 13
            ),
            user = UserFactory.sample(id = "admin@boclips.com")
        )

        val updatedVideo = videoRepository.find(videoId)!!

        assertThat(updatedVideo.ageRange).isEqualTo(LowerBoundedAgeRange(13))
    }
}
