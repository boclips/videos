package com.boclips.contentpartner.service.application.contentpartner

import com.boclips.contentpartner.service.application.channel.BroadcastChannels
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.eventbus.events.contentpartner.BroadcastChannelRequested
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BroadcastChannelsIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var broadcastChannels: BroadcastChannels

    @Test
    fun `dispatches an event for every channel`() {
        saveContentPartner(
            name = "First Content Partner"
        )

        saveContentPartner(
            name = "Second Content Partner"
        )

        saveContentPartner(
            name = "Third Content Partner"
        )

        broadcastChannels()

        val events = fakeEventBus.getEventsOfType(BroadcastChannelRequested::class.java)
        assertThat(events).hasSize(3)
    }
}
