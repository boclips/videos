package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GetChannelsTest : AbstractSpringIntegrationTest() {
    @Test
    fun `can fetch channels by name`() {
        val channel1 = saveChannel(name = "hello")
        saveChannel(name = "good night")

        val channels = getChannels.invoke(name = "hello").elements

        val returnedChannelIds = channels.map { it.id }
        assertThat(returnedChannelIds).containsExactly(channel1.id)
    }
}
