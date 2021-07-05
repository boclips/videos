package com.boclips.videos.service.application.video

import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.channel.AgeRangeRequest
import com.boclips.videos.service.domain.model.OpenEndedAgeRange
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.CategoryFactory
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

        createAgeRange(
            AgeRangeRequest(
                id = "thirteen-plus",
                min = 13,
                label = "13+"
            )
        )

        updateVideo(
            id = videoId.value,
            updateRequest = VideoServiceApiFactory.createUpdateVideoRequest(ageRangeMin = 13),
            user = UserFactory.sample(id = "admin@boclips.com")
        )

        val updatedVideo = videoRepository.find(videoId)!!

        assertThat(updatedVideo.ageRange).isEqualTo(OpenEndedAgeRange(min = 13, curatedManually = true))
    }

    @Test
    fun `updates video with additional description`() {
        val videoId = saveVideo(additionalDescription = "additional description")

        updateVideo(
            id = videoId.value,
            updateRequest = VideoServiceApiFactory.createUpdateVideoRequest(additionalDescription = "updated description"),
            user = UserFactory.sample(id = "admin@boclips.com")
        )

        val updatedVideo = videoRepository.find(videoId)!!

        assertThat(updatedVideo.additionalDescription).isEqualTo("updated description")
    }

    @Test
    fun `updates video with taxonomy categories`() {
        taxonomyRepository.create(CategoryFactory.sample(code = "A", description = "A description"))
        taxonomyRepository.create(
            CategoryFactory.sample(
                code = "ABC",
                description = "ABC description",
                parentCode = "AB"
            )
        )
        taxonomyRepository.create(
            CategoryFactory.sample(
                code = "C",
                description = "C description",
            )
        )

        val videoId = saveVideo(manualCategories = listOf("A", "ABC"))

        updateVideo(
            id = videoId.value,
            updateRequest = VideoServiceApiFactory.createUpdateVideoRequest(categories = listOf("C")),
            user = UserFactory.sample(id = "admin@boclips.com")
        )

        val updatedVideo = videoRepository.find(videoId)!!

        assertThat(updatedVideo.categories[CategorySource.MANUAL]).hasSize(1)
        assertThat(updatedVideo.categories[CategorySource.MANUAL]).contains(
            CategoryWithAncestors(
                CategoryCode("C"),
                "C description"
            )
        )
    }
}
