package com.boclips.videos.service.domain.model

fun convertAgeRange(ageRange: AgeRange): com.boclips.search.service.domain.videos.model.AgeRange {
    return when (ageRange) {
        is FixedAgeRange -> com.boclips.search.service.domain.videos.model.AgeRange(
            min = ageRange.min,
            max = ageRange.max
        )
        is OpenEndedAgeRange -> com.boclips.search.service.domain.videos.model.AgeRange(
            min = ageRange.min
        )
        is CappedAgeRange -> com.boclips.search.service.domain.videos.model.AgeRange(
            max = ageRange.max
        )
        UnknownAgeRange -> com.boclips.search.service.domain.videos.model.AgeRange()
    }
}