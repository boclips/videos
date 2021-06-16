package com.boclips.contentpartner.service.application.contract.legalrestrictions

import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestrictionsRepository
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.eventbus.events.contractlegalrestriction.ContractLegalRestrictionBroadcastRequested
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BroadcastContractLegalRestrictionsIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var broadcastContractLegalRestrictions: BroadcastContractLegalRestrictions

    @Autowired
    lateinit var contractLegalRestrictionsRepository: ContractLegalRestrictionsRepository

    @Test
    fun `broadcasts all legal restrictions`() {
        contractLegalRestrictionsRepository.create("restriction 1")
        contractLegalRestrictionsRepository.create("restriction 2")

        broadcastContractLegalRestrictions()

        val events = fakeEventBus.getEventsOfType(ContractLegalRestrictionBroadcastRequested::class.java)
        assertThat(events).hasSize(2)
        assertThat(events.map { it.contractLegalRestriction.text }).containsExactlyInAnyOrder(
            "restriction 1",
            "restriction 2"
        )
    }
}
