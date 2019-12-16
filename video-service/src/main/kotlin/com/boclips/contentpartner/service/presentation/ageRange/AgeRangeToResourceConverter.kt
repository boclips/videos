package com.boclips.contentpartner.service.presentation.ageRange

import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.domain.model.BoundedAgeRange
import com.boclips.contentpartner.service.domain.model.UnboundedAgeRange

class AgeRangeToResourceConverter {
    companion object {
        fun convert(
            ageRange: AgeRange?
        ): AgeRangeResource? {
            return when (ageRange) {
                is BoundedAgeRange -> AgeRangeResource(
                    ageRange.min,
                    ageRange.max
                )
                is UnboundedAgeRange -> null
                else -> null
            }
        }
    }
}