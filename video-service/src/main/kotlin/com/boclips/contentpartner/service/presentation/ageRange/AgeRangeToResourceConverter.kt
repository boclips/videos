package com.boclips.contentpartner.service.presentation.ageRange

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.videos.api.response.channel.AgeRangeBucketsResource

class AgeRangeToResourceConverter {
    companion object {
        fun convert(
            ageRange: AgeRangeBuckets
        ): AgeRangeBucketsResource {
            return AgeRangeBucketsResource(min = ageRange.min, max = ageRange.max, ids = ageRange.ids.map { it.value })
        }
    }
}
