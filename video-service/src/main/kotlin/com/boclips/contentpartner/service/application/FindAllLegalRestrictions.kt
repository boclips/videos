package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.presentation.LegalRestrictionsToResourceConverter
import com.boclips.videos.api.response.contentpartner.LegalRestrictionsResource

class FindAllLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {

    operator fun invoke(): List<LegalRestrictionsResource> {
        return legalRestrictionsRepository.findAll()
            .map { LegalRestrictionsToResourceConverter().convert(it) }
    }
}
