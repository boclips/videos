package com.boclips.contentpartner.service.domain.service.contentpartnercontract

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractUpdateResult
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contract.ContractUpdated

class ContentPartnerContractRepositoryEventDecorator(
    private val contentPartnerContractRepository: ContentPartnerContractRepository,
    private val eventConverter: EventConverter,
    private val eventBus: EventBus
) : ContentPartnerContractRepository by contentPartnerContractRepository {
    override fun create(contract: ContentPartnerContract): ContentPartnerContract {
        return contentPartnerContractRepository.create(contract)
    }

    override fun findById(id: ContentPartnerContractId): ContentPartnerContract? {
        return contentPartnerContractRepository.findById(id)
    }

    override fun findAll(pageRequest: PageRequest): ResultsPage<ContentPartnerContract> {
        return contentPartnerContractRepository.findAll(pageRequest)
    }

    override fun update(contentPartnerContractUpdateCommands: List<ContentPartnerContractUpdateCommand>): List<ContractUpdateResult> {
        val updateResults = contentPartnerContractRepository.update(contentPartnerContractUpdateCommands)

        publishContractsUpdated(updateResults.map { it.contract })

        return updateResults
    }

    override fun findAllByIds(contractIds: List<ContentPartnerContractId>): List<ContentPartnerContract> {
        return contentPartnerContractRepository.findAllByIds(contractIds)
    }

    private fun publishContractsUpdated(contracts: List<ContentPartnerContract>) {
        contracts.map(this::publishContractUpdate)
    }

    private fun publishContractUpdate(contract: ContentPartnerContract) {
        eventBus.publish(
            ContractUpdated.builder()
                .contract(eventConverter.toContractPayload(contract))
                .build()
        )
    }
}

