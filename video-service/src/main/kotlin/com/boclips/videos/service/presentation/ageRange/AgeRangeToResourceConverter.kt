package com.boclips.videos.service.presentation.ageRange

import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.ageRange.BoundedAgeRange
import com.boclips.videos.service.domain.model.ageRange.UnboundedAgeRange

class AgeRangeToResourceConverter {
    fun convert(
        ageRange: AgeRange?
    ): AgeRangeResource? {
        return when (ageRange) {
            is BoundedAgeRange -> AgeRangeResource(ageRange.min, ageRange.max)
            is UnboundedAgeRange -> null
            else -> null
        }
    }
}