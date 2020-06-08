package com.boclips.contentpartner.service.application.contract

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository

class GetContracts(private val contractRepository: ContractRepository) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }

    operator fun invoke(page: Int?, size: Int?): ResultsPage<Contract> {
        return contractRepository.findAll(
            pageRequest = PageRequest(
                page = page ?: 0,
                size = size ?: DEFAULT_PAGE_SIZE
            )
        )
    }
}
