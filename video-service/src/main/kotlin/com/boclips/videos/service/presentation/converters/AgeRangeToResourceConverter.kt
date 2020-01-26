package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.agerange.AgeRangeResource
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.BoundedAgeRange
import com.boclips.videos.service.domain.model.UnboundedAgeRange

class AgeRangeToResourceConverter {
    companion object {
        fun convert(ageRange: AgeRange?): AgeRangeResource? = when (ageRange) {
            is BoundedAgeRange -> AgeRangeResource(
                ageRange.min,
                ageRange.max
            )
            is UnboundedAgeRange -> null
            else -> null
        }
    }
}
