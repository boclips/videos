package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.contentpartner.AgeRangeRequest
import com.boclips.videos.api.request.contentpartner.LegalRestrictionsRequest
import com.boclips.videos.api.request.contentpartner.UpsertContentPartnerRequest
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ContentPartnerUpdatesConverterTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var contentPartnerUpdatesConverter: ContentPartnerUpdatesConverter

    @Autowired
    lateinit var legalRestrictionsRepository: LegalRestrictionsRepository

    lateinit var originalContentPartner: ContentPartner

    @BeforeEach
    fun setUp() {
        originalContentPartner = createContentPartner(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "My content partner"
            )
        )
    }

    @Test
    fun `creates command for updating distribution methods`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                name = "Hello",
                distributionMethods = setOf(DistributionMethodResource.DOWNLOAD)
            )
        )
        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceDistributionMethods } as ContentPartnerUpdateCommand.ReplaceDistributionMethods

        assertThat(command.distributionMethods).isEqualTo(setOf(DistributionMethod.DOWNLOAD))
    }

    @Test
    fun `creates command for updating the name`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                name = "Hello"
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceName } as ContentPartnerUpdateCommand.ReplaceName

        assertThat(command.name).isEqualTo("Hello")
        assertThat(command.contentPartnerId.value).isEqualTo(originalContentPartner.contentPartnerId.value)
    }

    @Test
    fun `creates command for updating the age range`() {
        createAgeRange(AgeRangeRequest(id = "early-years", label = "label", min = 1, max = 3))

        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                ageRanges = listOf("early-years"),
                name = null,
                accreditedToYtChannelId = "test"
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceAgeRanges } as ContentPartnerUpdateCommand.ReplaceAgeRanges

        assertThat(command.ageRangeBuckets.max).isEqualTo(3)
        assertThat(command.ageRangeBuckets.min).isEqualTo(1)
    }

    @Test
    fun `creates command for updating legal restrictions`() {
        val legalRestrictions = legalRestrictionsRepository.create("No restrictions")

        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                legalRestrictions = LegalRestrictionsRequest(
                    id = legalRestrictions.id.value
                )
            )
        )

        assertThat(commands).hasSize(1)

        assertThat(commands[0]).isInstanceOfSatisfying(ContentPartnerUpdateCommand.ReplaceLegalRestrictions::class.java) { command ->
            assertThat(command.contentPartnerId).isEqualTo(originalContentPartner.contentPartnerId)
            assertThat(command.legalRestriction).isEqualTo(legalRestrictions)
        }
    }

    @Test
    fun `creates command for updating content partner types`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                contentTypes = listOf("NEWS", "STOCK", "INSTRUCTIONAL")
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceContentTypes } as ContentPartnerUpdateCommand.ReplaceContentTypes

        assertThat(command.contentType).containsExactlyInAnyOrder("NEWS", "STOCK", "INSTRUCTIONAL")
    }

    @Test
    fun `creates command for updating content partner categories`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                contentCategories = listOf("DOCUMENTARY_SHORTS", "ANIMATION")
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceContentCategories } as ContentPartnerUpdateCommand.ReplaceContentCategories

        assertThat(command.contentCategories).containsExactlyInAnyOrder("DOCUMENTARY_SHORTS", "ANIMATION")
    }

    @Test
    fun `creates command for updating content partner language`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                language = "spa"
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceLanguage } as ContentPartnerUpdateCommand.ReplaceLanguage

        assertThat(command.language).contains("spa")
    }

    @Test
    fun `creates command for updating content partner description`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                description = "This is a new description"
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceDescription } as ContentPartnerUpdateCommand.ReplaceDescription

        assertThat(command.description).contains("This is a new description")
    }

    @Test
    fun `creates command for updating content partner awards`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                awards = "This is a new award"
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceAwards } as ContentPartnerUpdateCommand.ReplaceAwards

        assertThat(command.awards).contains("This is a new award")
    }

    @Test
    fun `creates command for updating content partner hubspot id`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                hubspotId = "1a2s3d4f5g6h7j8k9l"
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceHubspotId } as ContentPartnerUpdateCommand.ReplaceHubspotId

        assertThat(command.hubspotId).contains("1a2s3d4f5g6h7j8k9l")
    }

    @Test
    fun `creates command for updating content partner notes`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                notes = "this is a note"
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceNotes } as ContentPartnerUpdateCommand.ReplaceNotes

        assertThat(command.notes).contains("this is a note")
    }

    @Test
    fun `creates command for updating content partner transcript value`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                isTranscriptProvided = true
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceIsTranscriptProvided } as ContentPartnerUpdateCommand.ReplaceIsTranscriptProvided

        assertThat(command.isTranscriptProvided).isTrue()
    }

    @Test
    fun `creates command for updating content partner educational resources`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                educationalResources = "This is a resource"
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceEducationalResources } as ContentPartnerUpdateCommand.ReplaceEducationalResources

        assertThat(command.educationalResources).contains("This is a resource")
    }

    @Test
    fun `creates command for updating content partner best for tags`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                bestForTags = listOf("123", "456")
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceBestForTags } as ContentPartnerUpdateCommand.ReplaceBestForTags

        assertThat(command.bestForTags).containsExactlyInAnyOrder("123", "456")
    }

    @Test
    fun `creates command for updating content partner curriculum`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                curriculumAligned = "curriculum"
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceCurriculumAligned } as ContentPartnerUpdateCommand.ReplaceCurriculumAligned

        assertThat(command.curriculumAligned).contains("curriculum")
    }

    @Test
    fun `creates command for updating content partner subjects`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                subjects = listOf("subject 1", "subject 2")
            )
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceSubjects } as ContentPartnerUpdateCommand.ReplaceSubjects

        assertThat(command.subjects).containsExactlyInAnyOrder("subject 1", "subject 2")
    }
}
