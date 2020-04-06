package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.newlegalrestriction.NewLegalRestriction
import com.boclips.videos.api.request.newlegalrestrictions.NewLegalRestrictionResource
import com.boclips.videos.api.request.newlegalrestrictions.SingleLegalRestriction
import com.boclips.videos.api.response.newlegalrestriction.LegalRestrictionsWrapperResource
import com.boclips.videos.api.response.newlegalrestriction.NewLegalRestrictionsResource
import com.boclips.videos.api.response.newlegalrestriction.SingleLegalRestrictionResponse

class NewLegalRestrictionsToResourceConverter {
    operator fun invoke(legalRestrictions: List<NewLegalRestriction>): NewLegalRestrictionsResource {
        return NewLegalRestrictionsResource(_embedded = legalRestrictions.map {
            LegalRestrictionsWrapperResource(
                id = it.id,
                restrictions = it.restrictions.map { restriction ->
                    SingleLegalRestrictionResponse(
                        id = restriction.id,
                        text = restriction.text
                    )
                }
            )
        })
    }

    fun toSingleResource(legalRestriction: NewLegalRestriction): NewLegalRestrictionResource {
        return NewLegalRestrictionResource(
            id = legalRestriction.id,
            restrictions = legalRestriction.restrictions.map {
                SingleLegalRestriction(id = it.id, text = it.text)
            }
        )
    }
}

