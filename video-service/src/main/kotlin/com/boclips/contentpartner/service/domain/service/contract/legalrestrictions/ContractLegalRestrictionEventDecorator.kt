package com.boclips.contentpartner.service.domain.service.contract.legalrestrictions

import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestriction
import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestrictionsRepository
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contractlegalrestriction.ContractLegalRestrictionUpdated
import com.boclips.eventbus.domain.ContractLegalRestriction as ContractLegalRestrictionEvent

class ContractLegalRestrictionsEventDecorator(
    private val legalRestrictionsRepository: ContractLegalRestrictionsRepository,
    private val eventBus: EventBus
) : ContractLegalRestrictionsRepository by legalRestrictionsRepository {
    override fun create(text: String): ContractLegalRestriction {
        val legalRestriction = legalRestrictionsRepository.create(text)

        val updateEvent =
            ContractLegalRestrictionUpdated.builder()
                .contractLegalRestriction(
                    ContractLegalRestrictionEvent.builder()
                        .id(legalRestriction.id)
                        .text(legalRestriction.text)
                        .build()
                ).build()

        eventBus.publish(updateEvent)

        return legalRestriction
    }
}