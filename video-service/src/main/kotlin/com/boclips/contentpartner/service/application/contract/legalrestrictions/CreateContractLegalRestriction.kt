package com.boclips.contentpartner.service.application.contract.legalrestrictions

import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestriction
import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestrictionsRepository
import com.boclips.videos.api.request.contract.legalrestrictions.CreateContractLegalRestrictionRequest

class CreateContractLegalRestriction(private val contractLegalRestrictions: ContractLegalRestrictionsRepository) {
    operator fun invoke(request: CreateContractLegalRestrictionRequest): ContractLegalRestriction {
        return contractLegalRestrictions.create(
            text = request.text
        )
    }
}
