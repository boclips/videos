package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GetChannelsTest : AbstractSpringIntegrationTest() {
    @Test
    fun `can fetch content partners by name`() {
        val contentPartner1 = saveContentPartner(name = "hello")
        saveContentPartner(name = "good night")

        val contentPartners = getChannels.invoke(name = "hello", official = null)

        val returnedContentPartnerIds = contentPartners.map { it.id }
        assertThat(returnedContentPartnerIds).containsExactly(contentPartner1.id)
    }

    @Test
    fun `can fetch content partners by officiality`() {
        saveContentPartner(name = "Youtube CP Name", accreditedToYtChannel = "1234")
        val officialContentPartner = saveContentPartner(name = "CP Name", accreditedToYtChannel = null)

        val contentPartners = getChannels.invoke(official = true)

        val returnedContentPartnerIds = contentPartners.map { it.id }
        assertThat(returnedContentPartnerIds).containsExactly(officialContentPartner.id)
    }

    @Test
    fun `can fetch content partners by YT channel ID`() {
        saveContentPartner(name = "cp-1", accreditedToYtChannel = "1236")
        val contentPartnerWithYtId = saveContentPartner(name = "cp-2", accreditedToYtChannel = "1234")

        val contentPartners = getChannels.invoke(accreditedToYtChannelId = "1234")

        val returnedContentPartnerIds = contentPartners.map { it.id }
        assertThat(returnedContentPartnerIds).containsExactly(contentPartnerWithYtId.id)
    }
}
