package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.AgeRange
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
            ),
            contentPartner = originalContentPartner
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
            ),
            contentPartner = originalContentPartner
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceName } as ContentPartnerUpdateCommand.ReplaceName

        assertThat(command.name).isEqualTo("Hello")
        assertThat(command.contentPartnerId.value).isEqualTo(originalContentPartner.contentPartnerId.value)
    }

    @Test
    fun `creates command for updating the age range`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = originalContentPartner.contentPartnerId,
            upsertContentPartnerRequest = UpsertContentPartnerRequest(
                ageRange = AgeRangeRequest(1, 3),
                name = null,
                accreditedToYtChannelId = "test"
            ),
            contentPartner = originalContentPartner
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceAgeRange } as ContentPartnerUpdateCommand.ReplaceAgeRange

        assertThat(command.ageRange).isEqualTo(AgeRange.bounded(1, 3))
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
            ),
            contentPartner = originalContentPartner
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
            ),
            contentPartner = originalContentPartner
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
            ),
            contentPartner = originalContentPartner
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
            ),
            contentPartner = originalContentPartner
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
            ),
            contentPartner = originalContentPartner
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
            ),
            contentPartner = originalContentPartner
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
            ),
            contentPartner = originalContentPartner
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
            ),
            contentPartner = originalContentPartner
        )

        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceNotes } as ContentPartnerUpdateCommand.ReplaceNotes

        assertThat(command.notes).contains("this is a note")
    }
}
