package com.boclips.contentpartner.service.application.newlegalrestriction

import com.boclips.contentpartner.service.domain.model.newlegalrestriction.NewLegalRestriction
import com.boclips.contentpartner.service.domain.model.newlegalrestriction.NewLegalRestrictionsRepository

class FindAllNewLegalRestrictions(private val newLegalRestrictions: NewLegalRestrictionsRepository) {
    operator fun invoke(): List<NewLegalRestriction> {
        return newLegalRestrictions.findAll()
    }
}