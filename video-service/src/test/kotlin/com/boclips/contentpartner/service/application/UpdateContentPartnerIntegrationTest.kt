package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.MrssFeedIngest
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.contentpartner.ContentPartnerMarketingInformationRequest
import com.boclips.videos.api.request.contentpartner.LegalRestrictionsRequest
import com.boclips.videos.api.request.contentpartner.ContentPartnerRequest
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import com.boclips.videos.api.response.contentpartner.IngestDetailsResource
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Period

class UpdateContentPartnerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateContentPartner: UpdateContentPartner

    @Autowired
    lateinit var contentPartnerRepository: ContentPartnerRepository

    @Autowired
    lateinit var videoRepository: VideoRepository

    lateinit var originalContentPartner: ContentPartner

    lateinit var videoId: VideoId

    @BeforeEach
    fun setUp() {
        originalContentPartner = createContentPartner(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "My content partner",
                distributionMethods = emptySet(),
                legalRestrictions = null,
                marketingInformation = ContentPartnerMarketingInformationRequest(
                    showreel = Specified("http://www.server.com/showreel.mov")
                )
            )
        )
        videoId = saveVideo(
            contentProvider = "My content partner"
        )
    }

    @Test
    fun `content partner gets updated`() {
        val legalRestrictionsId = saveLegalRestrictions(text = "Legal restrictions")
        updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            upsertRequest = ContentPartnerRequest(
                name = "My better content partner",
                distributionMethods = setOf(
                    DistributionMethodResource.STREAM,
                    DistributionMethodResource.DOWNLOAD
                ),
                legalRestrictions = LegalRestrictionsRequest(
                    id = legalRestrictionsId.value
                ),
                ingest = IngestDetailsResource.mrss("https://mrss.feed.com"),
                deliveryFrequency = Period.ofMonths(4)
            )
        )

        val updatedContentPartner = contentPartnerRepository.findById(originalContentPartner.contentPartnerId)!!
        assertThat(updatedContentPartner.name).isEqualTo("My better content partner")
        assertThat(updatedContentPartner.legalRestriction).isNotNull
        assertThat(updatedContentPartner.legalRestriction?.id).isEqualTo(legalRestrictionsId)
        assertThat(updatedContentPartner.legalRestriction?.text).isEqualTo("Legal restrictions")
        assertThat(updatedContentPartner.ingest).isEqualTo(MrssFeedIngest("https://mrss.feed.com"))
        assertThat(updatedContentPartner.deliveryFrequency?.months).isEqualTo(4)
    }

    @Test
    fun `emits event`() {
        val description = "Test description"
        val contentCategories = listOf("WITH_A_HOST")
        val contentTypes = listOf("NEWS")
        val notes = "This is an interesting CP"
        val hubspotId = "12345678"

        updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            upsertRequest = ContentPartnerRequest(
                distributionMethods = setOf(
                    DistributionMethodResource.STREAM,
                    DistributionMethodResource.DOWNLOAD
                ),
                ingest = IngestDetailsResource.mrss("https://feed.me"),
                deliveryFrequency = Period.ofYears(1),
                language = "spa",
                description = description,
                contentCategories = contentCategories,
                contentTypes = contentTypes,
                notes = notes,
                hubspotId = hubspotId
            )
        )

        assertThat(fakeEventBus.countEventsOfType(ContentPartnerUpdated::class.java)).isEqualTo(1)

        val event = fakeEventBus.getEventOfType(ContentPartnerUpdated::class.java)

        assertThat(event.contentPartner.id.value).isEqualTo(originalContentPartner.contentPartnerId.value)
        assertThat(event.contentPartner.language.isO3Language).isEqualTo("spa")
        assertThat(event.contentPartner.description).isEqualTo(description)
        assertThat(event.contentPartner.contentCategories).isEqualTo(contentCategories)
        assertThat(event.contentPartner.contentTypes).isEqualTo(contentTypes)
        assertThat(event.contentPartner.notes).isEqualTo(notes)
        assertThat(event.contentPartner.hubspotId).isEqualTo(hubspotId)
        assertThat(event.contentPartner.ingest.type).isEqualTo("MRSS")
        assertThat(event.contentPartner.ingest.url).isEqualTo("https://feed.me")
        assertThat(event.contentPartner.deliveryFrequency).isEqualTo(Period.ofYears(1))
    }

    @Test
    fun `legal restrictions get created when id not set`() {
        updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            upsertRequest = ContentPartnerRequest(
                legalRestrictions = LegalRestrictionsRequest(
                    id = "",
                    text = "New legal restrictions"
                )
            )
        )

        val updatedContentPartner = contentPartnerRepository.findById(originalContentPartner.contentPartnerId)!!
        assertThat(updatedContentPartner.legalRestriction).isNotNull
        assertThat(updatedContentPartner.legalRestriction?.id?.value).isNotBlank()
        assertThat(updatedContentPartner.legalRestriction?.text).isEqualTo("New legal restrictions")
    }

    @Test
    fun `marketing showreel preserved when showreel is updated to null`() {
        updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            upsertRequest = ContentPartnerRequest(
                marketingInformation = ContentPartnerMarketingInformationRequest(
                    showreel = null
                )
            )
        )

        val updatedContentPartner = contentPartnerRepository.findById(
            originalContentPartner.contentPartnerId
        )!!
        assertThat(updatedContentPartner.marketingInformation?.showreel).isEqualTo(
            originalContentPartner.marketingInformation?.showreel
        )
    }

    @Test
    fun `marketing showreel preserved when showreel is updated with a value of ExplicitlyNull`() {
        updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            upsertRequest = ContentPartnerRequest(
                marketingInformation = ContentPartnerMarketingInformationRequest(
                    showreel = ExplicitlyNull()
                )
            )
        )

        val updatedContentPartner = contentPartnerRepository.findById(
            originalContentPartner.contentPartnerId
        )!!
        assertThat(updatedContentPartner.marketingInformation?.showreel).isNull()
    }
}
