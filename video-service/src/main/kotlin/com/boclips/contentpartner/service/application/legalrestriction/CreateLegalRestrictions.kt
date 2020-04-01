package com.boclips.contentpartner.service.application.legalrestriction

import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository

class CreateLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {
    operator fun invoke(text: String?): LegalRestriction {
        return legalRestrictionsRepository.create(text!!)
    }
}
