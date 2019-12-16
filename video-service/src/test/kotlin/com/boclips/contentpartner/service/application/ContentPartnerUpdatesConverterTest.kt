package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.presentation.ContentPartnerRequest
import com.boclips.contentpartner.service.presentation.LegalRestrictionsRequest
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.contentpartner.service.presentation.DistributionMethodResource
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ContentPartnerUpdatesConverterTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var contentPartnerUpdatesConverter: ContentPartnerUpdatesConverter

    @Autowired
    lateinit var legalRestrictionsRepository: LegalRestrictionsRepository

    @Test
    fun `creates command for updating distribution methods`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = ContentPartnerId(value = "123"),
            contentPartnerRequest = ContentPartnerRequest(
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
            id = ContentPartnerId(value = "123"),
            contentPartnerRequest = ContentPartnerRequest(
                name = "Hello",
                ageRange = null
            )
        )
        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceName } as ContentPartnerUpdateCommand.ReplaceName

        assertThat(command.name).isEqualTo("Hello")
        assertThat(command.contentPartnerId.value).isEqualTo("123")
    }

    @Test
    fun `creates command for updating the age range`() {
        val commands = contentPartnerUpdatesConverter.convert(
            id = ContentPartnerId(value = "123"),
            contentPartnerRequest = ContentPartnerRequest(
                ageRange = AgeRangeRequest(1, 3),
                name = null,
                accreditedToYtChannelId = "test"
            )
        )
        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceAgeRange } as ContentPartnerUpdateCommand.ReplaceAgeRange

        assertThat(command.ageRange).isEqualTo(AgeRange.bounded(1, 3))
    }

    @Test
    fun `creates command for updating legal restrictions`() {
        val legalRestrictions = legalRestrictionsRepository.create("No restrictions")
        val commands = contentPartnerUpdatesConverter.convert(
            id = ContentPartnerId(value = "123"),
            contentPartnerRequest = ContentPartnerRequest(
                legalRestrictions = LegalRestrictionsRequest(id = legalRestrictions.id.value)
            )
        )

        assertThat(commands).hasSize(1)
        assertThat(commands[0]).isInstanceOfSatisfying(ContentPartnerUpdateCommand.ReplaceLegalRestrictions::class.java) { command ->
            assertThat(command.contentPartnerId).isEqualTo(ContentPartnerId("123"))
            assertThat(command.legalRestrictions).isEqualTo(legalRestrictions)
        }
    }

    @Test
    fun `creates command for updating the currency`() {
        val commands = ContentPartnerUpdatesConverter(legalRestrictionsRepository).convert(
            id = ContentPartnerId(value = "123"),
            contentPartnerRequest = ContentPartnerRequest(
                currency = "GBP"
            )
        )
        val command =
            commands.find { it is ContentPartnerUpdateCommand.ReplaceCurrency } as ContentPartnerUpdateCommand.ReplaceCurrency

        assertThat(command.currency.currencyCode).isEqualTo("GBP")
    }
}
