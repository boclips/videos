package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerUpdateCommand
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentPartnerUpdatesConverterTest {
    @Test
    fun `creates command for setting searchability`() {
        val commands = ContentPartnerUpdatesConverter().convert(
            id = ContentPartnerId(value = "123"),
            contentPartnerRequest = ContentPartnerRequest(name = "Hello", searchable = true)
        )
        val command =
            commands.find { it is ContentPartnerUpdateCommand.SetSearchability } as ContentPartnerUpdateCommand.SetSearchability

        assertThat(command.searchable).isEqualTo(true)
    }

    @Test
    fun `creates command for updating delivery methods`() {
        val commands = ContentPartnerUpdatesConverter().convert(
            id = ContentPartnerId(value = "123"),
            contentPartnerRequest = ContentPartnerRequest(
                name = "Hello",
                hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.DOWNLOAD)
            )
        )
        val command =
            commands.find { it is ContentPartnerUpdateCommand.SetHiddenDeliveryMethods } as ContentPartnerUpdateCommand.SetHiddenDeliveryMethods

        assertThat(command.methods).isEqualTo(setOf(DeliveryMethod.DOWNLOAD))
    }

    @Test
    fun `creates command for updating the name`() {
        val commands = ContentPartnerUpdatesConverter().convert(
            id = ContentPartnerId(value = "123"),
            contentPartnerRequest = ContentPartnerRequest(name = "Hello", ageRange = null)
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
}