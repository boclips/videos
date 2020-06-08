package com.boclips.contentpartner.service.presentation.converters.contracts

import com.boclips.contentpartner.service.domain.model.contract.ContractRestrictions
import com.boclips.videos.api.request.contract.ContractRestrictionsRequest

class ContractRestrictionsConverter {
    fun fromResource(restrictions: ContractRestrictionsRequest): ContractRestrictions {
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
