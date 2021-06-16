package com.boclips.contentpartner.service.domain.service.contract

import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.contract.ContractFilter
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand
import com.boclips.contentpartner.service.domain.model.contract.CreateContractResult
import com.boclips.contentpartner.service.domain.model.contract.SingleContractUpdate
import com.boclips.contentpartner.service.domain.model.contract.UpdateContractResult

class ContractService(
    private val contractRepository: ContractRepository
) {
    fun create(contract: Contract): CreateContractResult {
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

    private fun findContractsWithConflictingNames(singleContractUpdate: SingleContractUpdate): List<Contract> {
        return singleContractUpdate
            .commands
            .filterIsInstance<ContractUpdateCommand.ReplaceContentPartnerName>()
            .takeIf { it.isNotEmpty() }
            ?.map { nameUpdates -> ContractFilter.NameFilter(nameUpdates.contentPartnerName) }
            ?.let { nameFilters -> contractRepository.findAll(nameFilters).toList() }
            ?.filter { it.id != singleContractUpdate.contractId }
            ?: emptyList()
    }

    private fun isNameConflict(contract: Contract): Boolean {
        return contractRepository
            .findAll(listOf(ContractFilter.NameFilter(contract.contentPartnerName)))
            .toList()
            .isNotEmpty()
    }
}
