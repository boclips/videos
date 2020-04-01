package com.boclips.contentpartner.service.application.legalrestriction

import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository

class FindAllLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {
    operator fun invoke(): List<LegalRestriction> {
        return legalRestrictionsRepository.findAll()
    }
}
