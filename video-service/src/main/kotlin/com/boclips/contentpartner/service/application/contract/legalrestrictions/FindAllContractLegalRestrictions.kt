package com.boclips.contentpartner.service.application.contract.legalrestrictions

import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestriction
import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestrictionsRepository

class FindAllContractLegalRestrictions(private val contractLegalRestrictions: ContractLegalRestrictionsRepository) {
    operator fun invoke(): List<ContractLegalRestriction> {
        return contractLegalRestrictions.findAll()
    }
}
