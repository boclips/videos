package com.boclips.contentpartner.service.domain.model.contract

sealed class CreateContractResult {
    data class Success(val contract: Contract) : CreateContractResult()
    data class NameConflict(val name: String) : CreateContractResult()
}
