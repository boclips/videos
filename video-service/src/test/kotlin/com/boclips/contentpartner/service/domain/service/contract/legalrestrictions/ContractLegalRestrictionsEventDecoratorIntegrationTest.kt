package com.boclips.contentpartner.service.domain.service.contract.legalrestrictions

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.eventbus.events.contractlegalrestriction.ContractLegalRestrictionUpdated
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ContractLegalRestrictionsEventDecoratorIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var legalRestrictions: ContractLegalRestrictionsEventDecorator

    @Test
    fun `publishes a ContractUpdate event on create`() {
        val restriction = legalRestrictions.create("legal bad videos")

        val event = fakeEventBus.getEventOfType(ContractLegalRestrictionUpdated::class.java)

        assertThat(event.contractLegalRestriction.id).isEqualTo(restriction.id)
        assertThat(event.contractLegalRestriction.text).isEqualTo("legal bad videos")
    }
}