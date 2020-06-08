package com.boclips.contentpartner.service.domain.service.contract

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contract.ContractUpdated

class ContractRepositoryEventDecorator(
    private val contractRepository: ContractRepository,
    private val eventConverter: EventConverter,
    private val eventBus: EventBus
) : ContractRepository by contractRepository {
    override fun create(contract: Contract): Contract {
        val created = contractRepository.create(contract)

        publishContractUpdate(created)

        return created
    }

    override fun findById(id: ContractId): Contract? {
        return contractRepository.findById(id)
    }

    override fun findAll(pageRequest: PageRequest): ResultsPage<Contract> {
        return contractRepository.findAll(pageRequest)
    }

    override fun update(contractUpdateCommands: List<ContractUpdateCommand>): List<Contract> {
        val updateResults = contractRepository.update(contractUpdateCommands)

        publishContractsUpdated(updateResults)

        return updateResults
    }

    override fun findAllByIds(contractIds: List<ContractId>): List<Contract> {
        return contractRepository.findAllByIds(contractIds)
    }

    private fun publishContractsUpdated(contracts: List<Contract>) {
        contracts.map(this::publishContractUpdate)
    }

    private fun publishContractUpdate(contract: Contract) {
        eventBus.publish(
            ContractUpdated.builder()
                .contract(eventConverter.toContractPayload(contract))
                .build()
        )
    }
}

