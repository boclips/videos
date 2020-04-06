package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.agerange.AgeRangeResource
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.LowerBoundedAgeRange
import com.boclips.videos.service.domain.model.SpecificAgeRange
import com.boclips.videos.service.domain.model.UnknownAgeRange
import com.boclips.videos.service.domain.model.UpperBoundedAgeRange

class AgeRangeToResourceConverter {
    companion object {
        fun convert(ageRange: AgeRange?): AgeRangeResource? = when (ageRange) {
            is SpecificAgeRange -> AgeRangeResource(ageRange.min, ageRange.max)
            is LowerBoundedAgeRange -> AgeRangeResource(ageRange.min, null)
            is UpperBoundedAgeRange -> AgeRangeResource(null, ageRange.max)
            is UnknownAgeRange -> null
            else -> null
        }
    }
}
