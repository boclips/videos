package com.boclips.contentpartner.service.application.legalrestriction

import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository

class FindLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {
    operator fun invoke(id: String): LegalRestriction? {
        return legalRestrictionsRepository.findById(
            LegalRestrictionsId(
                id
            )
        )
    }
}
