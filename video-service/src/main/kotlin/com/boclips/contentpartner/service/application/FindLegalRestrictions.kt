package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.LegalRestriction
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository

class FindLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {
    operator fun invoke(id: String): LegalRestriction? {
        return legalRestrictionsRepository.findById(
            LegalRestrictionsId(
                id
            )
        )
    }
}
