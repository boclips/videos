package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestriction
import com.boclips.videos.api.response.contract.legalrestrictions.ContractLegalRestrictionResource
import com.boclips.videos.api.response.contract.legalrestrictions.ContractLegalRestrictionsResource
import com.boclips.videos.api.response.contract.legalrestrictions.LegalRestrictionsWrapperResource

class ContractLegalRestrictionsToResourceConverter {
    operator fun invoke(legalRestrictions: List<ContractLegalRestriction>): ContractLegalRestrictionsResource {
        return ContractLegalRestrictionsResource(
            _embedded = LegalRestrictionsWrapperResource(
                restrictions = legalRestrictions.map {
                    ContractLegalRestrictionResource(id = it.id, text = it.text)
                }
            )
        )
    }
}
