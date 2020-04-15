package com.boclips.contentpartner.service.presentation.converters.contracts

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractRestrictions
import com.boclips.videos.api.request.contract.ContentPartnerContractRestrictionsRequest

class ContractRestrictionsConverter {
    fun fromResource(restrictions: ContentPartnerContractRestrictionsRequest): ContractRestrictions {
        return restrictions.let {
            ContractRestrictions(
                clientFacing = it.clientFacing,
                territory = it.territory,
                licensing = it.licensing,
                editing = it.editing,
                marketing = it.marketing,
                companies = it.companies,
                payout = it.payout,
                other = it.other
            )
        }
    }
}