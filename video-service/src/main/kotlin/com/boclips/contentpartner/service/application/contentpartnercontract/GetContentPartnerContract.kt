package com.boclips.contentpartner.service.application.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository

class GetContentPartnerContract(
    private val contentPartnerContractRepository: ContentPartnerContractRepository
) {
    operator fun invoke(id: ContentPartnerContractId): ContentPartnerContract? =
        contentPartnerContractRepository.findById(id)
}
