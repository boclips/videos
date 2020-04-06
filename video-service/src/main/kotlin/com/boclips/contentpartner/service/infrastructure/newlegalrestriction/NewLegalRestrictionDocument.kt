package com.boclips.contentpartner.service.infrastructure.newlegalrestriction

import com.boclips.contentpartner.service.domain.model.newlegalrestriction.NewLegalRestriction
import com.boclips.contentpartner.service.domain.model.newlegalrestriction.SingleLegalRestriction

data class NewLegalRestrictionDocument(
    val id: String,
    val restrictions: List<SingleLegalRestriction>
) {
    fun toNewLegalRestriction(): NewLegalRestriction {
        return NewLegalRestriction(
            id = id,
            restrictions = restrictions
        )
    }

    companion object {
        fun toNewLegalRestrictionDocument(newLegalRestriction: NewLegalRestriction): NewLegalRestrictionDocument {
            return NewLegalRestrictionDocument(
                id = newLegalRestriction.id,
                restrictions = newLegalRestriction.restrictions
            )
        }
    }
}