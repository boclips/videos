package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.presentation.LegalRestrictionsToResourceConverter
import com.boclips.videos.api.response.contentpartner.LegalRestrictionsResource

class FindLegalRestrictions(private val legalRestrictionsRepository: LegalRestrictionsRepository) {

    operator fun invoke(id: String): LegalRestrictionsResource? {
        return legalRestrictionsRepository.findById(
            LegalRestrictionsId(
                id
            )
        )?.let { LegalRestrictionsToResourceConverter().convert(it) }
    }
}
