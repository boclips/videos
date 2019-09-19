package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository

class FindAllLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {

    operator fun invoke(): List<LegalRestrictionsResource> {
        return legalRestrictionsRepository.findAll()
            .map { LegalRestrictionsResource.from(it) }
    }
}
