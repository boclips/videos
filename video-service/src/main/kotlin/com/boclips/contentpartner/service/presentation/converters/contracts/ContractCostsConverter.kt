package com.boclips.contentpartner.service.presentation.converters.contracts

import com.boclips.contentpartner.service.domain.model.contract.ContractCosts
import com.boclips.videos.api.request.contract.ContractCostsRequest

class ContractCostsConverter {
    fun fromResource(costs: ContractCostsRequest): ContractCosts {
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
