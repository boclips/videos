package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.video.request.FixedAgeRangeFacet

fun convertAgeRange(ageRange: FixedAgeRangeFacet): com.boclips.search.service.domain.videos.model.AgeRange {
    return com.boclips.search.service.domain.videos.model.AgeRange(
        min = ageRange.min,
        max = ageRange.max
    )
}

fun convertAgeRange(ageRange: AgeRange): com.boclips.search.service.domain.videos.model.AgeRange {
    return com.boclips.search.service.domain.videos.model.AgeRange(
        min = ageRange.min(),
        max = ageRange.max()
    )
}