package com.boclips.contentpartner.service.domain.model.contentpartnercontract

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage

interface ContentPartnerContractRepository {
    fun create(contract: ContentPartnerContract): ContentPartnerContractId
    fun findById(id: ContentPartnerContractId): ContentPartnerContract?
    fun findAll(pageRequest: PageRequest): ResultsPage<ContentPartnerContract>
}