package com.boclips.contentpartner.service.presentation.converters.contracts

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractCosts
import com.boclips.videos.api.request.contract.ContentPartnerContractCostsRequest

class ContractCostsConverter {
    fun fromResource(costs: ContentPartnerContractCostsRequest): ContractCosts {
        return costs.let {
            ContractCosts(
                minimumGuarantee = it.minimumGuarantee,
                upfrontLicense = it.upfrontLicense,
                technicalFee = it.technicalFee,
                recoupable = it.recoupable
            )
        }
    }
}