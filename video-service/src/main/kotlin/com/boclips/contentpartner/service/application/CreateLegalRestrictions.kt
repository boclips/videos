package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository

class CreateLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {

    operator fun invoke(text: String?): LegalRestrictionsResource {
        val restrictions = legalRestrictionsRepository.create(text!!)
        return LegalRestrictionsResource.from(restrictions)
    }
}
