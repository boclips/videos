package com.boclips.contentpartner.service.presentation.ageRange

import com.boclips.contentpartner.service.domain.model.AgeRangeBuckets
import com.boclips.videos.api.response.contentpartner.AgeRangeResource

class AgeRangeToResourceConverter {
    companion object {
        fun convert(
            ageRange: AgeRangeBuckets
        ): AgeRangeResource {
            return AgeRangeResource(min = ageRange.min, max = ageRange.max, ids = ageRange.ids.map { it.value })
        }
    }
}
