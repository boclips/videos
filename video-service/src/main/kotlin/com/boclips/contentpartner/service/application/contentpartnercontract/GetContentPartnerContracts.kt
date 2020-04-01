package com.boclips.contentpartner.service.application.contentpartnercontract

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository

class GetContentPartnerContracts(private val contentPartnerContractRepository: ContentPartnerContractRepository) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }

    operator fun invoke(page: Int?, size: Int?): ResultsPage<ContentPartnerContract> {
        return contentPartnerContractRepository.findAll(
            pageRequest = PageRequest(
                page = page ?: 0,
                size = size ?: DEFAULT_PAGE_SIZE
            )
        )
    }
}
