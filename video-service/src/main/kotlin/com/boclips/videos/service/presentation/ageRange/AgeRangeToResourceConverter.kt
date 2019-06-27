package com.boclips.videos.service.presentation.ageRange

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.common.BoundedAgeRange
import com.boclips.videos.service.domain.model.common.UnboundedAgeRange

class AgeRangeToResourceConverter {
    companion object {

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
}