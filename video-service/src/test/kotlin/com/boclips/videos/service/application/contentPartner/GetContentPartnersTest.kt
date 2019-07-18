package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GetContentPartnersTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getContentPartners: GetContentPartners

    @Test
    fun `can fetch content partners by name`() {
        val contentPartner1 = saveContentPartner(name = "hello")
        val contentPartner2 = saveContentPartner(name = "hello")
        saveContentPartner(name = "good night")

        val contentPartners = getContentPartners.invoke(name = "hello", official = null)

        val returnedContentPartnerIds = contentPartners.content.map { it.content.id }
        assertThat(returnedContentPartnerIds).containsExactly(
            contentPartner1.contentPartnerId.value,
            contentPartner2.contentPartnerId.value
        )
    }

    @Test
    fun `can fetch content partners by officiality`() {
        saveContentPartner(accreditedToYtChannel = "1234")
        saveContentPartner(accreditedToYtChannel = "1234")
        val officialContentPartner = saveContentPartner(accreditedToYtChannel = null)

        val contentPartners = getContentPartners.invoke(official = true)

        val returnedContentPartnerIds = contentPartners.content.map { it.content.id }
        assertThat(returnedContentPartnerIds).containsExactly(officialContentPartner.contentPartnerId.value)
    }

    @Test
    fun `can fetch content partners by YT channel ID`() {
        saveContentPartner(accreditedToYtChannel = "1236")
        val contentPartnerWithYtId = saveContentPartner(accreditedToYtChannel = "1234")

        val contentPartners = getContentPartners.invoke(accreditedToYtChannelId = "1234")

        val returnedContentPartnerIds = contentPartners.content.map { it.content.id }
        assertThat(returnedContentPartnerIds).containsExactly(contentPartnerWithYtId.contentPartnerId.value)
    }
}