package com.boclips.videos.service.domain.model

fun convertAgeRange(ageRange: AgeRange): com.boclips.search.service.domain.videos.model.AgeRange {
    return when (ageRange) {
        is SpecificAgeRange -> com.boclips.search.service.domain.videos.model.AgeRange(
            min = ageRange.min,
            max = ageRange.max
        )
        is LowerBoundedAgeRange -> com.boclips.search.service.domain.videos.model.AgeRange(
            min = ageRange.min
        )
        is UpperBoundedAgeRange -> com.boclips.search.service.domain.videos.model.AgeRange(
            max = ageRange.max
        )
        UnknownAgeRange -> com.boclips.search.service.domain.videos.model.AgeRange()
    }
}