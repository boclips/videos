package com.boclips.contentpartner.service.domain.model

interface ContentPartnerContractRepository {
    fun create(contract: ContentPartnerContract): ContentPartnerContractId
    fun findById(id: ContentPartnerContractId): ContentPartnerContract?
}
