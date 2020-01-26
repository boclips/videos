package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.LegalRestriction
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository

class CreateLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {
    operator fun invoke(text: String?): LegalRestriction {
        return legalRestrictionsRepository.create(text!!)
    }
}
