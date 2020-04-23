package com.boclips.contentpartner.service.domain.model.contentpartnercontract

sealed class CreateContractResult() {
    data class Success(val contract: ContentPartnerContract) : CreateContractResult()
    data class NameConflict(val name: String) : CreateContractResult()
}
