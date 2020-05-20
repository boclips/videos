package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.contract.ContractUpdated
import mu.KLogging

class ContractUpdated(
    private val contractRepository: ContentPartnerContractRepository,
    private val channelRepository: ChannelRepository
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun contractUpdated(contractUpdatedEvent: ContractUpdated) {
        logger.info { "Updating channels for contract: <${contractUpdatedEvent.contract.name}:${contractUpdatedEvent.contract.contractId.value}>" }

        val contractId = ContentPartnerContractId(contractUpdatedEvent.contract.contractId.value)

        val contract = contractRepository.findById(contractId)

        if (contract == null) {
            logger.warn { "Could not find contract ${contractUpdatedEvent.contract.contractId.value}" }
            return
        }

        val updateContractCommands = channelRepository.findByContractId(contractId).map {
            ChannelUpdateCommand.ReplaceContract(
                contract = contract,
                channelId = it.id
            )
        }

        channelRepository.update(updateCommands = updateContractCommands)

        logger.info { "Finished updating content partners for contract: $contract" }
    }
}
