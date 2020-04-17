package com.boclips.contentpartner.service.domain.model.contentpartnercontract

data class ContractUpdateResult(val contract: ContentPartnerContract, val commands: List<ContentPartnerContractUpdateCommand>)
