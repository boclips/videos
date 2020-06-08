package com.boclips.contentpartner.service.domain.model.contract

data class SingleContractUpdate(
    val contractId: ContractId,
    val commands: List<ContractUpdateCommand>
)
