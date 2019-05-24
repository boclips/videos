package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CreateContentPartnerTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contentPartnerRepository: ContentPartnerRepository

    @Autowired
    lateinit var videoService : VideoService

    @Autowired
    lateinit var createContentPartner: CreateContentPartner

    @Test
    fun `creates a content partner and sets the default age for their videos`() {
        val videoId = saveVideo(contentProvider = "My content partner")

        createContentPartner(TestFactories.createCreateContentPartnerRequest(
            name = "My content partner",
            ageRange = AgeRangeRequest(min = 7, max = 11)
        ))

        val contentPartner = contentPartnerRepository.findByName(contentPartnerName = "My content partner")
        val video = videoService.getPlayableVideo(videoId = videoId)

        assertThat(contentPartner!!.name).isEqualTo("My content partner")
        assertThat(video.ageRange.min()).isEqualTo(7)
        assertThat(video.ageRange.max()).isEqualTo(11)
    }
}