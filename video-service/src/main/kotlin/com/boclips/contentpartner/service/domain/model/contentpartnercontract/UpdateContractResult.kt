package com.boclips.contentpartner.service.domain.model.contentpartnercontract

sealed class UpdateContractResult {
    data class Success(val contract: ContentPartnerContract) : UpdateContractResult()
    data class NameConflict(val name: String): UpdateContractResult()
    data class MissingContract(val contractId: ContentPartnerContractId): UpdateContractResult()
}
