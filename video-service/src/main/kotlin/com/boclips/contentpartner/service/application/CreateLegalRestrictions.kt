package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.presentation.LegalRestrictionsToResourceConverter
import com.boclips.videos.api.response.contentpartner.LegalRestrictionsResource

class CreateLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {

    operator fun invoke(text: String?): LegalRestrictionsResource {
        val restrictions = legalRestrictionsRepository.create(text!!)
        return LegalRestrictionsToResourceConverter().convert(restrictions)
    }
}
