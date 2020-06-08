package com.boclips.contentpartner.service.application.contract

import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository

class GetContract(
    private val contractRepository: ContractRepository
) {
    operator fun invoke(id: ContractId): Contract? =
        contractRepository.findById(id)
}
