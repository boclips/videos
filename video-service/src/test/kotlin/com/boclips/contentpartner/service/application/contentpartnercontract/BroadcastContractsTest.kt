package com.boclips.contentpartner.service.application.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.eventbus.events.contract.BroadcastContractRequested
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BroadcastContractsTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contractRepository: ContentPartnerContractRepository

    @Autowired
    lateinit var broadcastContracts: BroadcastContracts

    @Test
    fun `dispatches an event for every contract`() {
        listOf("first", "second").map {
            contractRepository.create(ContentPartnerContractFactory.sample(contentPartnerName = it))
        }

        broadcastContracts()

        val events = fakeEventBus.getEventsOfType(BroadcastContractRequested::class.java)
        assertThat(events).hasSize(2)
    }
}