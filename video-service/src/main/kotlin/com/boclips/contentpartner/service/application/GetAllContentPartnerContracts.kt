package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.ContentPartnerContractRepository

class GetAllContentPartnerContracts(val contentPartnerContractRepository: ContentPartnerContractRepository) {
    operator fun invoke(): List<ContentPartnerContract> {
        return contentPartnerContractRepository.findAll()
    }
}
