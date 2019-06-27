package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.common.UnboundedAgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CreateContentPartnerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contentPartnerRepository: ContentPartnerRepository

    @Autowired
    lateinit var videoService: VideoService

    @Test
    fun `creates a content partner and does not overwrite video age ranges`() {
        val videoId = saveVideo(
            contentProvider = "My content partner",
            ageRange = UnboundedAgeRange
        )

        createContentPartner(
            TestFactories.createContentPartnerRequest(
                name = "My content partner",
                ageRange = AgeRangeRequest(min = 7, max = 11)
            )
        )

        val contentPartner = contentPartnerRepository.findByName(contentPartnerName = "My content partner")

        val video = videoService.getPlayableVideo(videoId = videoId)
        assertThat(contentPartner!!.name).isEqualTo("My content partner")
        assertThat(video.ageRange).isInstanceOf(UnboundedAgeRange::class.java)
    }
}