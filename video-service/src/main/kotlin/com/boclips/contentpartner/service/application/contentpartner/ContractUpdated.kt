package com.boclips.contentpartner.service.application.contentpartner

import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerUpdateCommand
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.contract.ContractUpdated
import mu.KLogging

class ContractUpdated(
    private val contractRepository: ContentPartnerContractRepository,
    private val contentPartnerRepository: ContentPartnerRepository
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun contractUpdated(contractUpdatedEvent: ContractUpdated) {
        logger.info { "Updating content partners for contract: <${contractUpdatedEvent.contract.name}:${contractUpdatedEvent.contract.contractId.value}>" }

        val contractId = ContentPartnerContractId(contractUpdatedEvent.contract.contractId.value)

        val contract = contractRepository.findById(contractId)

        if (contract == null) {
            logger.warn { "Could not find contract ${contractUpdatedEvent.contract.contractId.value}" }
            return
        }

        val updateContractCommands = contentPartnerRepository.findByContractId(contractId).map {
            ContentPartnerUpdateCommand.ReplaceContract(
                contract = contract,
                contentPartnerId = it.contentPartnerId
            )
        }

        contentPartnerRepository.update(updateCommands = updateContractCommands)

        logger.info { "Finished updating content partners for contract: $contract" }
    }
}
