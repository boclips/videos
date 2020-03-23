package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.ContentPartnerContractRepository

class GetContentPartnerContract(
    private val contentPartnerContractRepository: ContentPartnerContractRepository
) {
    operator fun invoke(id: ContentPartnerContractId): ContentPartnerContract? =
        contentPartnerContractRepository.findById(id)
}