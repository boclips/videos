package com.boclips.videos.service.application.legal.restrictions

import com.boclips.videos.service.domain.model.legal.restrictions.LegalRestrictionsRepository

class CreateLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {

    operator fun invoke(text: String?): LegalRestrictionsResource {
        val restrictions = legalRestrictionsRepository.create(text!!)
        return LegalRestrictionsResource.from(restrictions)
    }
}