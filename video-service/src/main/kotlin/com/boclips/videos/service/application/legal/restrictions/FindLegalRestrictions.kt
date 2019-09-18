package com.boclips.videos.service.application.legal.restrictions

import com.boclips.videos.service.domain.model.legal.restrictions.LegalRestrictionsId
import com.boclips.videos.service.domain.model.legal.restrictions.LegalRestrictionsRepository

class FindLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {

    operator fun invoke(id: String): LegalRestrictionsResource? {
        return legalRestrictionsRepository.findById(LegalRestrictionsId(id))?.let { LegalRestrictionsResource.from(it) }
    }
}