package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.LegalRestrictions
import com.boclips.videos.api.response.contentpartner.LegalRestrictionsResource

class LegalRestrictionsToResourceConverter {
    fun convert(legalRestrictions: LegalRestrictions): LegalRestrictionsResource {
        return LegalRestrictionsResource(
            id = legalRestrictions.id.value,
            text = legalRestrictions.text
        )
    }
}
