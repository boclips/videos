package com.boclips.contentpartner.service.application.contentpartnercontract.legalrestrictions

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.legalrestrictions.ContractLegalRestriction
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.legalrestrictions.ContractLegalRestrictionsRepository

class FindAllContractLegalRestrictions(private val contractLegalRestrictions: ContractLegalRestrictionsRepository) {
    operator fun invoke(): List<ContractLegalRestriction> {
        return contractLegalRestrictions.findAll()
    }
}