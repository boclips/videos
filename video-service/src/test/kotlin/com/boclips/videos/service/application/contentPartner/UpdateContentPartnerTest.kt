package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UpdateContentPartnerTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateContentPartner : UpdateContentPartner

    @Autowired
    lateinit var createContentPartner : CreateContentPartner

    @Autowired
    lateinit var contentPartnerRepository : ContentPartnerRepository

    @Autowired
    lateinit var videoService : VideoService

    @Test
    fun `updating a content partner`() {
        val videoId = saveVideo(contentProvider = "My better content partner")

        createContentPartner(
            TestFactories.createContentPartnerRequest(
                name = "My content partner",
                ageRange = AgeRangeRequest(min = 7, max = 11)
            ))

        updateContentPartner(
            existingContentPartnerName = "My content partner",
            request =
            TestFactories.createContentPartnerRequest(
                name = "My better content partner",
                ageRange = AgeRangeRequest(min = 9, max = 14)
            )
        )

        val deletedContentPartner = contentPartnerRepository.findByName(contentPartnerName = "My content partner")
        val updatedContentPartner = contentPartnerRepository.findByName(contentPartnerName = "My better content partner")
        val video = videoService.getPlayableVideo(videoId = videoId)

        assertThat(updatedContentPartner!!.name).isEqualTo("My better content partner")
        assertThat(video.ageRange.min()).isEqualTo(9)
        assertThat(video.ageRange.max()).isEqualTo(14)

        assertThat(deletedContentPartner).isNull()
    }
}