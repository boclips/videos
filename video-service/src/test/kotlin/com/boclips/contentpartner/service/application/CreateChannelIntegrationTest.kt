package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.exceptions.ChannelConflictException
import com.boclips.contentpartner.service.application.exceptions.ChannelHubspotIdException
import com.boclips.contentpartner.service.application.exceptions.InvalidAgeRangeException
import com.boclips.contentpartner.service.application.exceptions.InvalidContentCategoryException
import com.boclips.contentpartner.service.application.exceptions.InvalidContractException
import com.boclips.contentpartner.service.application.exceptions.MissingContractException
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.channel.YoutubeScrapeIngest
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.api.response.channel.IngestDetailsResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Period
import java.util.Locale
import kotlin.contracts.contract

class CreateChannelIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `channels are searchable everywhere by default`() {
        val channel = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                name = "My channel",
                distributionMethods = null
            )
        )

        assertThat(channel.distributionMethods).isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `mark channels available for stream and download`() {
        val channel = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                name = "My channel",
                distributionMethods = setOf(
                    DistributionMethodResource.DOWNLOAD,
                    DistributionMethodResource.STREAM
                )
            )
        )

        assertThat(channel.distributionMethods).isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `videos are searchable when distribution methods are not specified`() {
        val channel = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                name = "My channel",
                distributionMethods = null
            )
        )

        assertThat(channel.distributionMethods).isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `cannot create a channel without a contract`() {
        assertThrows<MissingContractException> {
            createChannel(
                VideoServiceApiFactory.createChannelRequest(
                    name = "Tsitsipas",
                    ingest = IngestDetailsResource.manual(),
                    contractId = null
                )
            )
        }
    }

    @Test
    fun `can create a channel without a contract when it's from a youtube source`() {
        val channel = createChannel(VideoServiceApiFactory.createChannelRequest(
            name = "Gregor Dimitrov",
            ingest = IngestDetailsResource.youtube()
        ))

        assertThat(channel.name).isEqualTo("Gregor Dimitrov")
    }


    @Test
    fun `cannot create a channel with an invalid content category`() {
        assertThrows<InvalidContentCategoryException> {
            createChannel(
                VideoServiceApiFactory.createChannelRequest(
                    contentCategories = listOf("non existent")
                )
            )
        }
    }

    @Test
    fun `can create a channel with a content category`() {
        val channelWithCategory = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                contentCategories = listOf("VIRTUAL_REALITY_360")
            )
        )

        assertThat(channelWithCategory.contentCategories).hasSize(1)
    }

    @Test
    fun `can create a channel with a selected language`() {
        val channelWithCategory = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                language = "spa"
            )
        )

        val languageTag = Locale.forLanguageTag("spa")
        assertThat(channelWithCategory.language).isEqualTo(languageTag)
    }

    @Test
    fun `can create a channel without a selected language`() {
        val channelWithCategory = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                name = "without language cp",
                language = null
            )
        )

        assertThat(channelWithCategory.name).isEqualTo("without language cp")
    }

    @Test
    fun `cannot create the same channel with the same name`() {
        createChannel(VideoServiceApiFactory.createChannelRequest())
        assertThrows<ChannelConflictException> {
            createChannel(
                VideoServiceApiFactory.createChannelRequest()
            )
        }
    }

    @Test
    fun `cannot create the same channel with the same hubspotId`() {
        createChannel(VideoServiceApiFactory.createChannelRequest(name = "old", hubspotId = "123"))
        assertThrows<ChannelHubspotIdException> {
            createChannel(
                VideoServiceApiFactory.createChannelRequest(name="new", hubspotId = "123")
            )
        }
    }

    @Test
    fun `cannot create a channel with an unrecognised age range bucket`() {
        assertThrows<InvalidAgeRangeException> {
            createChannel(
                VideoServiceApiFactory.createChannelRequest(
                    ageRanges = listOf("A missing age range")
                )
            )
        }
    }

    @Test
    fun `can create a channel with provided transcript `() {
        val isTranscriptProvided = true

        val channelWithTranscript = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                isTranscriptProvided = isTranscriptProvided
            )
        )

        assertThat(channelWithTranscript.pedagogyInformation?.isTranscriptProvided).isEqualTo(
            isTranscriptProvided
        )
    }

    @Test
    fun `can create a channel with educational resources`() {
        val educationalResources = "This is an educational resource"

        val channelWithEducationalResources = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                educationalResources = educationalResources
            )
        )

        assertThat(channelWithEducationalResources.pedagogyInformation?.educationalResources).isEqualTo(
            educationalResources
        )
    }

    @Test
    fun `can create a channel with curriculum aligned`() {
        val curriculumAligned = "This is a curriculum"

        val channelWithCurriculumAligned = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                curriculumAligned = curriculumAligned
            )
        )

        assertThat(channelWithCurriculumAligned.pedagogyInformation?.curriculumAligned).isEqualTo(
            curriculumAligned
        )
    }

    @Test
    fun `can create a channel with best for tags`() {
        val bestForTags = listOf("123", "345")

        val channelWithBestForTags = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                bestForTags = bestForTags
            )
        )

        assertThat(channelWithBestForTags.pedagogyInformation?.bestForTags).isEqualTo(bestForTags)
    }

    @Test
    fun `can create a channel with subjects`() {
        val subjects = listOf("subject 1", "subject 2")

        val channelWithBestForTags = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                subjects = subjects
            )
        )

        assertThat(channelWithBestForTags.pedagogyInformation?.subjects).isEqualTo(subjects)
    }

    @Test
    fun `can create a channel with delivery frequency`() {
        val channel = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                deliveryFrequency = Period.ofYears(1)
            )
        )

        assertThat(channel.deliveryFrequency).isEqualTo(Period.ofYears(1))
    }

    @Test
    fun `can create a channel with ingest information`() {
        val channel = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                ingest = IngestDetailsResource.youtube("https://yt.com/channel")
            )
        )

        assertThat(channel.ingest).isEqualTo(
            YoutubeScrapeIngest(
                listOf("https://yt.com/channel")
            )
        )
    }

    @Test
    fun `can create a channel with contract information`() {
        val contractId = saveContract(name = "hello", remittanceCurrency = "GBP").id
        val channel = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                contractId = contractId.value
            )
        )

        assertThat(channel.contract?.id).isEqualTo(contractId)
        assertThat(channel.contract?.contentPartnerName).isEqualTo("hello")
        assertThat(channel.contract?.remittanceCurrency?.currencyCode).isEqualTo("GBP")
    }

    @Test
    fun `emits event`() {
        val contractId = saveContract(name = "hello", remittanceCurrency = "GBP").id

        val description = "Test description"
        val contentCategories = listOf("WITH_A_HOST")
        val contentTypes = listOf("NEWS")
        val notes = "This is an interesting CP"
        val hubspotId = "12345678"

        val channel = createChannel(
            upsertRequest = ChannelRequest(
                name = "New Channel",
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
                hubspotId = hubspotId,
                contractId = contractId.value
            )
        )

        assertThat(fakeEventBus.countEventsOfType(ContentPartnerUpdated::class.java)).isEqualTo(1)

        val event = fakeEventBus.getEventOfType(ContentPartnerUpdated::class.java)

        assertThat(event.contentPartner.id.value).isEqualTo(channel.id.value)
        assertThat(event.contentPartner.details.language.isO3Language).isEqualTo("spa")
        assertThat(event.contentPartner.details.contentCategories).isEqualTo(contentCategories)
        assertThat(event.contentPartner.details.contentTypes).isEqualTo(contentTypes)
        assertThat(event.contentPartner.details.notes).isEqualTo(notes)
        assertThat(event.contentPartner.details.hubspotId).isEqualTo(hubspotId)
        assertThat(event.contentPartner.ingest.type).isEqualTo("MRSS")
        assertThat(event.contentPartner.ingest.deliveryFrequency).isEqualTo(Period.ofYears(1))
    }

    @Test
    fun `throws when contract does not exists`() {
        assertThrows<InvalidContractException> {
            createChannel(
                VideoServiceApiFactory.createChannelRequest(
                    contractId = "a contract"
                )
            )
        }
    }
}
