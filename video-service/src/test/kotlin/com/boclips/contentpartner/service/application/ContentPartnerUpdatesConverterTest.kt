package com.boclips.contentpartner.service.application

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.contentpartner.service.presentation.ContentPartnerRequest
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentPartnerUpdatesConverterTest {
    @Test
    fun `creates command for updating distribution methods`() {
        val commands = ContentPartnerUpdatesConverter().convert(
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
        val commands = ContentPartnerUpdatesConverter().convert(
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
        val commands = ContentPartnerUpdatesConverter().convert(
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
    fun `creates command for updating the currency`() {
        val commands = ContentPartnerUpdatesConverter().convert(
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
