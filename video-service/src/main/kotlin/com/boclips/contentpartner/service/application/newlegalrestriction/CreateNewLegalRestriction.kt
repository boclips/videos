package com.boclips.contentpartner.service.application.newlegalrestriction

import com.boclips.contentpartner.service.domain.model.newlegalrestriction.NewLegalRestriction
import com.boclips.contentpartner.service.domain.model.newlegalrestriction.NewLegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.model.newlegalrestriction.SingleLegalRestriction

class CreateNewLegalRestriction(private val newLegalRestrictions: NewLegalRestrictionsRepository) {
    operator fun invoke(id: String, restriction: List<SingleLegalRestriction>): NewLegalRestriction {
        return newLegalRestrictions.create(
            id = id,
            restrictions = restriction.map { SingleLegalRestriction(id = it.id, text = it.text) }
        )
    }
}