package com.boclips.contentpartner.service.domain.model.contentpartnercontract

data class SingleContractUpdate(
    val contractId: ContentPartnerContractId,
    val commands: List<ContentPartnerContractUpdateCommand>
)
