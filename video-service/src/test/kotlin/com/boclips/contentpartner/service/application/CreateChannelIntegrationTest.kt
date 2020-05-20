package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerConflictException
import com.boclips.contentpartner.service.application.exceptions.InvalidAgeRangeException
import com.boclips.contentpartner.service.application.exceptions.InvalidContentCategoryException
import com.boclips.contentpartner.service.application.exceptions.InvalidContractException
import com.boclips.contentpartner.service.application.exceptions.MissingContentPartnerContractException
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.channel.YoutubeScrapeIngest
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import com.boclips.videos.api.response.contentpartner.IngestDetailsResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Period
import java.util.Locale

class CreateChannelIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `content partners are searchable everywhere by default`() {
        val contentPartner = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "My content partner",
                distributionMethods = null
            )
        )

        assertThat(contentPartner.distributionMethods).isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `mark content partners available for stream and download`() {
        val contentPartner = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "My content partner",
                distributionMethods = setOf(
                    DistributionMethodResource.DOWNLOAD,
                    DistributionMethodResource.STREAM
                )
            )
        )

        assertThat(contentPartner.distributionMethods).isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `videos are searchable when distribution methods are not specified`() {
        val contentPartner = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "My content partner",
                distributionMethods = null
            )
        )

        assertThat(contentPartner.distributionMethods).isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `can create an official content partner with the same name as a youtube content partner`() {
        val youtubeContentPartner = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "Tsitsipas",
                accreditedToYtChannel = "23456789"
            )
        )

        val officialContentPartner = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "Tsitsipas",
                accreditedToYtChannel = null
            )
        )

        assertThat(officialContentPartner.id).isNotEqualTo(youtubeContentPartner)
    }

    @Test
    fun `cannot create an official content partner without a contract`() {
        assertThrows<MissingContentPartnerContractException> {
            createChannel(
                VideoServiceApiFactory.createContentPartnerRequest(
                    name = "Tsitsipas",
                    ingest = IngestDetailsResource.manual(),
                    contractId = null
                )
            )
        }
    }

    @Test
    fun `can create a youtube scrape content partner without a contract`() {
        val contractId = saveContentPartnerContract(name = "hello", remittanceCurrency = "GBP").id

        val youtubeContentPartner = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "Tsitsipas",
                accreditedToYtChannel = "23456789",
                contractId = contractId.value
            )
        )

        assertThat(youtubeContentPartner.id).isNotNull
    }

    @Test
    fun `cannot create a content partner with an invalid content category`() {
        assertThrows<InvalidContentCategoryException> {
            createChannel(
                VideoServiceApiFactory.createContentPartnerRequest(
                    contentCategories = listOf("non existent")
                )
            )
        }
    }

    @Test
    fun `can create a content partner with a content category`() {
        val contentPartnerWithCategory = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                contentCategories = listOf("VIRTUAL_REALITY_360")
            )
        )

        assertThat(contentPartnerWithCategory.contentCategories).hasSize(1)
    }

    @Test
    fun `can create a content partner with a selected language`() {
        val contentPartnerWithCategory = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                language = "spa"
            )
        )

        val languageTag = Locale.forLanguageTag("spa")
        assertThat(contentPartnerWithCategory.language).isEqualTo(languageTag)
    }

    @Test
    fun `can create a content partner without a selected language`() {
        val contentPartnerWithCategory = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "without language cp",
                language = null
            )
        )

        assertThat(contentPartnerWithCategory.name).isEqualTo("without language cp")
    }

    @Test
    fun `cannot create the same content partner with the same name`() {
        createChannel(VideoServiceApiFactory.createContentPartnerRequest())
        assertThrows<ContentPartnerConflictException> {
            createChannel(
                VideoServiceApiFactory.createContentPartnerRequest()
            )
        }
    }

    @Test
    fun `cannot create the same content partner with the same hubspotId`() {
        createChannel(VideoServiceApiFactory.createContentPartnerRequest(hubspotId = "123"))
        assertThrows<ContentPartnerConflictException> {
            createChannel(
                VideoServiceApiFactory.createContentPartnerRequest(hubspotId = "123")
            )
        }
    }

    @Test
    fun `cannot create a content partner with an unrecognised age range bucket`() {
        assertThrows<InvalidAgeRangeException> {
            createChannel(
                VideoServiceApiFactory.createContentPartnerRequest(
                    ageRanges = listOf("A missing age range")
                )
            )
        }
    }

    @Test
    fun `can create a content partner with provided transcript `() {
        val isTranscriptProvided = true

        val contentPartnerWithTranscript = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                isTranscriptProvided = isTranscriptProvided
            )
        )

        assertThat(contentPartnerWithTranscript.pedagogyInformation?.isTranscriptProvided).isEqualTo(
            isTranscriptProvided
        )
    }

    @Test
    fun `can create a content partner with educational resources`() {
        val educationalResources = "This is an educational resource"

        val contentPartnerWithEducationalResources = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                educationalResources = educationalResources
            )
        )

        assertThat(contentPartnerWithEducationalResources.pedagogyInformation?.educationalResources).isEqualTo(
            educationalResources
        )
    }

    @Test
    fun `can create a content partner with curriculum aligned`() {
        val curriculumAligned = "This is a curriculum"

        val contentPartnerWithCurriculumAligned = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                curriculumAligned = curriculumAligned
            )
        )

        assertThat(contentPartnerWithCurriculumAligned.pedagogyInformation?.curriculumAligned).isEqualTo(
            curriculumAligned
        )
    }

    @Test
    fun `can create a content partner with best for tags`() {
        val bestForTags = listOf("123", "345")

        val contentPartnerWithBestForTags = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                bestForTags = bestForTags
            )
        )

        assertThat(contentPartnerWithBestForTags.pedagogyInformation?.bestForTags).isEqualTo(bestForTags)
    }

    @Test
    fun `can create a content partner with subjects`() {
        val subjects = listOf("subject 1", "subject 2")

        val contentPartnerWithBestForTags = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                subjects = subjects
            )
        )

        assertThat(contentPartnerWithBestForTags.pedagogyInformation?.subjects).isEqualTo(subjects)
    }

    @Test
    fun `can create a content partner with delivery frequency`() {
        val contentPartner = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                deliveryFrequency = Period.ofYears(1)
            )
        )

        assertThat(contentPartner.deliveryFrequency).isEqualTo(Period.ofYears(1))
    }

    @Test
    fun `can create a content partner with ingest information`() {
        val contentPartner = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                ingest = IngestDetailsResource.youtube("https://yt.com/channel")
            )
        )

        assertThat(contentPartner.ingest).isEqualTo(
            YoutubeScrapeIngest(
                listOf("https://yt.com/channel")
            )
        )
    }

    @Test
    fun `can create a content partner with contract information`() {
        val contractId = saveContentPartnerContract(name = "hello", remittanceCurrency = "GBP").id
        val contentPartner = createChannel(
            VideoServiceApiFactory.createContentPartnerRequest(
                contractId = contractId.value
            )
        )

        assertThat(contentPartner.contract?.id).isEqualTo(contractId)
        assertThat(contentPartner.contract?.contentPartnerName).isEqualTo("hello")
        assertThat(contentPartner.contract?.remittanceCurrency?.currencyCode).isEqualTo("GBP")
    }

    @Test
    fun `throws when contract does not exists`() {
        assertThrows<InvalidContractException> {
            createChannel(
                VideoServiceApiFactory.createContentPartnerRequest(
                    contractId = "a contract"
                )
            )
        }
    }
}
