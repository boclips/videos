package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.agerange.AgeRangeResource
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.CappedAgeRange
import com.boclips.videos.service.domain.model.FixedAgeRange
import com.boclips.videos.service.domain.model.OpenEndedAgeRange
import com.boclips.videos.service.domain.model.UnknownAgeRange

class AgeRangeToResourceConverter {
    companion object {
        fun convert(ageRange: AgeRange?): AgeRangeResource? = when (ageRange) {
            is FixedAgeRange -> AgeRangeResource(ageRange.min, ageRange.max)
            is OpenEndedAgeRange -> AgeRangeResource(ageRange.min, null)
            is CappedAgeRange -> AgeRangeResource(null, ageRange.max)
            is UnknownAgeRange -> null
            else -> null
        }
    }
}
