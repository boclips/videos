package com.boclips.contentpartner.service.domain.model.contract

sealed class UpdateContractResult {
    data class Success(val contract: Contract) : UpdateContractResult()
    data class NameConflict(val name: String): UpdateContractResult()
    data class MissingContract(val contractId: ContractId): UpdateContractResult()
}
