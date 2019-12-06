package com.boclips.contentpartner.service.application

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GetContentPartnersTest : AbstractSpringIntegrationTest() {
    @Test
    fun `can fetch content partners by name`() {
        val contentPartner1 = saveContentPartner(name = "hello")
        saveContentPartner(name = "good night")

        val user = UserFactory.sample()
        val contentPartners = getContentPartners.invoke(user = user, name = "hello", official = null)

        val returnedContentPartnerIds = contentPartners.content.map { it.content.id }
        assertThat(returnedContentPartnerIds).containsExactly(
            contentPartner1.contentPartnerId.value
        )
    }

    @Test
    fun `can fetch content partners by officiality`() {
        saveContentPartner(name = "Youtube CP Name", accreditedToYtChannel = "1234")
        val officialContentPartner = saveContentPartner(name = "CP Name", accreditedToYtChannel = null)

        val user = UserFactory.sample()
        val contentPartners = getContentPartners.invoke(user = user, official = true)

        val returnedContentPartnerIds = contentPartners.content.map { it.content.id }
        assertThat(returnedContentPartnerIds).containsExactly(officialContentPartner.contentPartnerId.value)
    }

    @Test
    fun `can fetch content partners by YT channel ID`() {
        saveContentPartner(name = "cp-1", accreditedToYtChannel = "1236")
        val contentPartnerWithYtId = saveContentPartner(name = "cp-2", accreditedToYtChannel = "1234")

        val user = UserFactory.sample()
        val contentPartners = getContentPartners.invoke(user = user, accreditedToYtChannelId = "1234")

        val returnedContentPartnerIds = contentPartners.content.map { it.content.id }
        assertThat(returnedContentPartnerIds).containsExactly(contentPartnerWithYtId.contentPartnerId.value)
    }
}
