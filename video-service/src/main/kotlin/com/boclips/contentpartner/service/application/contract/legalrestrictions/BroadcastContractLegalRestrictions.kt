package com.boclips.contentpartner.service.application.contract.legalrestrictions

import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestrictionsRepository
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.domain.ContractLegalRestriction
import com.boclips.eventbus.events.contractlegalrestriction.ContractLegalRestrictionBroadcastRequested
import mu.KLogging

class BroadcastContractLegalRestrictions(
    private val eventBus: EventBus,
    private val contractLegalRestrictionsRepository: ContractLegalRestrictionsRepository
) {
    operator fun invoke() {
        logger.info { "Dispatching contract legal restrictions broadcast events" }
        val events = contractLegalRestrictionsRepository.findAll().map { contractRestriction ->
            ContractLegalRestrictionBroadcastRequested.builder()
                .contractLegalRestriction(
                    ContractLegalRestriction
                        .builder()
                        .id(contractRestriction.id)
                        .text(contractRestriction.text)
                        .build()
                )
                .build()
        }

        if (events.isNotEmpty()) {
            eventBus.publish(events)
        }
    }

    companion object : KLogging()
}
