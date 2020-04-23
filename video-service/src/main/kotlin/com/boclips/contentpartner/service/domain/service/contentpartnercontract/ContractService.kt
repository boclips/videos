package com.boclips.contentpartner.service.domain.service.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractFilter
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.CreateContractResult
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.SingleContractUpdate
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.UpdateContractResult

class ContractService(
    private val contractRepository: ContentPartnerContractRepository
) {
    fun create(contract: ContentPartnerContract): CreateContractResult {
        if (isNameConflict(contract)) {
            return CreateContractResult.NameConflict(contract.contentPartnerName)
        }

        return CreateContractResult.Success(contractRepository.create(contract))
    }

    fun update(singleContractUpdate: SingleContractUpdate): UpdateContractResult {
        if (contractRepository.findById(singleContractUpdate.contractId) == null) {
            return UpdateContractResult.MissingContract(singleContractUpdate.contractId)
        }

        val nameConflicts = findContractsWithConflictingNames(singleContractUpdate = singleContractUpdate)

        if (nameConflicts.isNotEmpty()) {
            return UpdateContractResult.NameConflict(nameConflicts.first().contentPartnerName)
        }

        return UpdateContractResult.Success(
            contractRepository.update(singleContractUpdate.commands)
                .first()
        )
    }

    private fun findContractsWithConflictingNames(singleContractUpdate: SingleContractUpdate): List<ContentPartnerContract> {
        return singleContractUpdate
            .commands
            .filterIsInstance<ContentPartnerContractUpdateCommand.ReplaceContentPartnerName>()
            .takeIf { it.isNotEmpty() }
            ?.map { nameUpdates -> ContractFilter.NameFilter(nameUpdates.contentPartnerName) }
            ?.let { nameFilters -> contractRepository.findAll(nameFilters).toList() }
            ?.filter { it.id != singleContractUpdate.contractId }
            ?: emptyList()
    }

    private fun isNameConflict(contract: ContentPartnerContract): Boolean {
        return contractRepository
            .findAll(listOf(ContractFilter.NameFilter(contract.contentPartnerName)))
            .toList()
            .isNotEmpty()
    }
}
