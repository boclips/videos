package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.LegalRestriction
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository

class FindAllLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {
    operator fun invoke(): List<LegalRestriction> {
        return legalRestrictionsRepository.findAll()
    }
}
