package com.boclips.contentpartner.service.domain.model.contract

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage

interface ContractRepository {
    fun create(contract: Contract): Contract
    fun findById(id: ContractId): Contract?
    fun findAllByIds(contractIds: List<ContractId>): List<Contract>
    fun findAll(pageRequest: PageRequest): ResultsPage<Contract>
    fun findAll(filters: List<ContractFilter>): Iterable<Contract>
    fun streamAll(consumer: (Sequence<Contract>) -> Unit)
    fun update(contractUpdateCommands: List<ContractUpdateCommand>): List<Contract>
}
