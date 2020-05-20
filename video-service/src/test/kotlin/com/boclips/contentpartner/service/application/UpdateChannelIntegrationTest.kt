package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.channel.UpdateChannel
import com.boclips.contentpartner.service.application.exceptions.InvalidContractException
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.MrssFeedIngest
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.contentpartner.ContentPartnerMarketingInformationRequest
import com.boclips.videos.api.request.contentpartner.ContentPartnerRequest
import com.boclips.videos.api.request.contentpartner.LegalRestrictionsRequest
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import com.boclips.videos.api.response.contentpartner.IngestDetailsResource
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Period

class UpdateChannelIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateChannel: UpdateChannel

    @Autowired
    lateinit var channelRepository: ChannelRepository

    @Autowired
    lateinit var videoRepository: VideoRepository

    lateinit var originalChannel: Channel

    lateinit var videoId: VideoId

    @BeforeEach
    fun setUp() {
        originalChannel = createChannel(
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
        updateChannel(
            channelId = originalChannel.id.value,
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

        val updatedContentPartner = channelRepository.findById(originalChannel.id)!!
        assertThat(updatedContentPartner.name).isEqualTo("My better content partner")
        assertThat(updatedContentPartner.legalRestriction).isNotNull
        assertThat(updatedContentPartner.legalRestriction?.id).isEqualTo(legalRestrictionsId)
        assertThat(updatedContentPartner.legalRestriction?.text).isEqualTo("Legal restrictions")
        assertThat(updatedContentPartner.ingest).isEqualTo(
            MrssFeedIngest(
                listOf("https://mrss.feed.com")
            )
        )
        assertThat(updatedContentPartner.deliveryFrequency?.months).isEqualTo(4)
    }

    @Test
    fun `emits event`() {
        val description = "Test description"
        val contentCategories = listOf("WITH_A_HOST")
        val contentTypes = listOf("NEWS")
        val notes = "This is an interesting CP"
        val hubspotId = "12345678"

        updateChannel(
            channelId = originalChannel.id.value,
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

        assertThat(event.contentPartner.id.value).isEqualTo(originalChannel.id.value)
        assertThat(event.contentPartner.details.language.isO3Language).isEqualTo("spa")
        assertThat(event.contentPartner.details.contentCategories).isEqualTo(contentCategories)
        assertThat(event.contentPartner.details.contentTypes).isEqualTo(contentTypes)
        assertThat(event.contentPartner.details.notes).isEqualTo(notes)
        assertThat(event.contentPartner.details.hubspotId).isEqualTo(hubspotId)
        assertThat(event.contentPartner.ingest.type).isEqualTo("MRSS")
        assertThat(event.contentPartner.ingest.deliveryFrequency).isEqualTo(Period.ofYears(1))
    }

    @Test
    fun `legal restrictions get created when id not set`() {
        updateChannel(
            channelId = originalChannel.id.value,
            upsertRequest = ContentPartnerRequest(
                legalRestrictions = LegalRestrictionsRequest(
                    id = "",
                    text = "New legal restrictions"
                )
            )
        )

        val updatedContentPartner = channelRepository.findById(originalChannel.id)!!
        assertThat(updatedContentPartner.legalRestriction).isNotNull
        assertThat(updatedContentPartner.legalRestriction?.id?.value).isNotBlank()
        assertThat(updatedContentPartner.legalRestriction?.text).isEqualTo("New legal restrictions")
    }

    @Test
    fun `marketing showreel preserved when showreel is updated to null`() {
        updateChannel(
            channelId = originalChannel.id.value,
            upsertRequest = ContentPartnerRequest(
                marketingInformation = ContentPartnerMarketingInformationRequest(
                    showreel = null
                )
            )
        )

        val updatedContentPartner = channelRepository.findById(
            originalChannel.id
        )!!
        assertThat(updatedContentPartner.marketingInformation?.showreel).isEqualTo(
            originalChannel.marketingInformation?.showreel
        )
    }

    @Test
    fun `marketing showreel preserved when showreel is updated with a value of ExplicitlyNull`() {
        updateChannel(
            channelId = originalChannel.id.value,
            upsertRequest = ContentPartnerRequest(
                marketingInformation = ContentPartnerMarketingInformationRequest(
                    showreel = ExplicitlyNull()
                )
            )
        )

        val updatedContentPartner = channelRepository.findById(
            originalChannel.id
        )!!
        assertThat(updatedContentPartner.marketingInformation?.showreel).isNull()
    }

    @Test
    fun `throws when contract id is invalid`() {
        assertThrows<InvalidContractException> {
            updateChannel(
                channelId = "irrelevant",
                upsertRequest = ContentPartnerRequest(contractId = "missing")
            )
        }
    }
}
