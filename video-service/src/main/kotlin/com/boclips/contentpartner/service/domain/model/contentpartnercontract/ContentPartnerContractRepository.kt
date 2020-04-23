package com.boclips.contentpartner.service.domain.model.contentpartnercontract

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage

interface ContentPartnerContractRepository {
    fun create(contract: ContentPartnerContract): ContentPartnerContract
    fun findById(id: ContentPartnerContractId): ContentPartnerContract?
    fun findAllByIds(contractIds: List<ContentPartnerContractId>): List<ContentPartnerContract>
    fun findAll(pageRequest: PageRequest): ResultsPage<ContentPartnerContract>
    fun findAll(filters: List<ContractFilter>): Iterable<ContentPartnerContract>
    fun update(contentPartnerContractUpdateCommands: List<ContentPartnerContractUpdateCommand>): List<ContentPartnerContract>
}
