package com.boclips.videos.service.application.legal.restrictions

import com.boclips.videos.service.domain.model.legal.restrictions.LegalRestrictionsRepository

class FindAllLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {

    operator fun invoke(): List<LegalRestrictionsResource> {
        return legalRestrictionsRepository.findAll()
            .map { LegalRestrictionsResource.from(it) }
    }
}