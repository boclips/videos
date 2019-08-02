package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GetContentPartnersTest : AbstractSpringIntegrationTest() {
    @Test
    fun `can fetch content partners by name`() {
        val contentPartner1 = saveContentPartner(name = "hello")
        saveContentPartner(name = "good night")

        val contentPartners = getContentPartners.invoke(name = "hello", official = null)

        val returnedContentPartnerIds = contentPartners.content.map { it.content.id }
        assertThat(returnedContentPartnerIds).containsExactly(
            contentPartner1.contentPartnerId.value
        )
    }

    @Test
    fun `can fetch content partners by officiality`() {
        saveContentPartner(name = "Youtube CP Name", accreditedToYtChannel = "1234")
        val officialContentPartner = saveContentPartner(name = "CP Name", accreditedToYtChannel = null)

        val contentPartners = getContentPartners.invoke(official = true)

        val returnedContentPartnerIds = contentPartners.content.map { it.content.id }
        assertThat(returnedContentPartnerIds).containsExactly(officialContentPartner.contentPartnerId.value)
    }

    @Test
    fun `can fetch content partners by YT channel ID`() {
        saveContentPartner(name = "cp-1", accreditedToYtChannel = "1236")
        val contentPartnerWithYtId = saveContentPartner(name = "cp-2", accreditedToYtChannel = "1234")

        val contentPartners = getContentPartners.invoke(accreditedToYtChannelId = "1234")

        val returnedContentPartnerIds = contentPartners.content.map { it.content.id }
        assertThat(returnedContentPartnerIds).containsExactly(contentPartnerWithYtId.contentPartnerId.value)
    }
}
