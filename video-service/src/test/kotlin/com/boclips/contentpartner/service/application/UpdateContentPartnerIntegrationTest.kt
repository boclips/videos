package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.UnboundedAgeRange
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.contentpartner.AgeRangeRequest
import com.boclips.videos.api.request.contentpartner.CreateContentPartnerRequest
import com.boclips.videos.api.request.contentpartner.LegalRestrictionsRequest
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UpdateContentPartnerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateContentPartner: UpdateContentPartner

    @Autowired
    lateinit var contentPartnerRepository: ContentPartnerRepository

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var videoService: VideoService

    lateinit var originalContentPartner: ContentPartner

    lateinit var videoId: VideoId

    @BeforeEach
    fun setUp() {
        originalContentPartner = createContentPartner(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "My content partner",
                ageRange = AgeRangeRequest(
                    min = 7,
                    max = 11
                ),
                distributionMethods = emptySet(),
                legalRestrictions = null
            )
        )
        videoId = saveVideo(
            contentProvider = "My content partner",
            ageRange = UnboundedAgeRange
        )
    }

    @Test
    fun `content partner gets updated`() {
        val legalRestrictionsId = saveLegalRestrictions(text = "Legal restrictions")
        updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            createRequest = CreateContentPartnerRequest(
                name = "My better content partner",
                ageRange = AgeRangeRequest(
                    min = 9,
                    max = 14
                ),
                distributionMethods = setOf(
                    DistributionMethodResource.STREAM,
                    DistributionMethodResource.DOWNLOAD
                ),
                legalRestrictions = LegalRestrictionsRequest(
                    id = legalRestrictionsId.value
                )
            )
        )

        val updatedContentPartner = contentPartnerRepository.findById(originalContentPartner.contentPartnerId)!!
        assertThat(updatedContentPartner.name).isEqualTo("My better content partner")
        assertThat(updatedContentPartner.ageRange.min()).isEqualTo(9)
        assertThat(updatedContentPartner.ageRange.max()).isEqualTo(14)
        assertThat(updatedContentPartner.legalRestrictions).isNotNull
        assertThat(updatedContentPartner.legalRestrictions?.id).isEqualTo(legalRestrictionsId)
        assertThat(updatedContentPartner.legalRestrictions?.text).isEqualTo("Legal restrictions")
    }

    @Test
    fun `emits event`() {
        updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            createRequest = CreateContentPartnerRequest(
                distributionMethods = setOf(
                    DistributionMethodResource.STREAM,
                    DistributionMethodResource.DOWNLOAD
                )
            )
        )

        assertThat(fakeEventBus.countEventsOfType(ContentPartnerUpdated::class.java)).isEqualTo(1)

        val event = fakeEventBus.getEventOfType(ContentPartnerUpdated::class.java)

        assertThat(event.contentPartner.id).isNotNull
    }

    @Test
    fun `legal restrictions get created when id not set`() {
        updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            createRequest = CreateContentPartnerRequest(
                legalRestrictions = LegalRestrictionsRequest(
                    id = "",
                    text = "New legal restrictions"
                )
            )
        )

        val updatedContentPartner = contentPartnerRepository.findById(originalContentPartner.contentPartnerId)!!
        assertThat(updatedContentPartner.legalRestrictions).isNotNull
        assertThat(updatedContentPartner.legalRestrictions?.id?.value).isNotBlank()
        assertThat(updatedContentPartner.legalRestrictions?.text).isEqualTo("New legal restrictions")
    }
}
