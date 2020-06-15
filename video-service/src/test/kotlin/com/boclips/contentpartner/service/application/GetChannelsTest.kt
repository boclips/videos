package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GetChannelsTest : AbstractSpringIntegrationTest() {
    @Test
    fun `can fetch content partners by name`() {
        val contentPartner1 = saveChannel(name = "hello")
        saveChannel(name = "good night")

        val contentPartners = getChannels.invoke(name = "hello")

        val returnedContentPartnerIds = contentPartners.map { it.id }
        assertThat(returnedContentPartnerIds).containsExactly(contentPartner1.id)
    }
}
