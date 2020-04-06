package com.boclips.contentpartner.service.application.newlegalrestriction

import com.boclips.contentpartner.service.domain.model.newlegalrestriction.NewLegalRestriction
import com.boclips.contentpartner.service.domain.model.newlegalrestriction.NewLegalRestrictionsRepository

class FindOneNewLegalRestriction(private val newLegalRestrictions: NewLegalRestrictionsRepository) {
    operator fun invoke(type: String): NewLegalRestriction? {
        return newLegalRestrictions.findOne(type)
    }
}