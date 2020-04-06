package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.application.exceptions.InvalidAgeRangeFormatException
import com.boclips.videos.service.domain.model.video.request.FixedAgeRangeFacet

fun convertAgeRangeFacets(rangeString: String): FixedAgeRangeFacet {
    try {
        val minAndMax = rangeString.split('-')
        return FixedAgeRangeFacet(min = minAndMax[0].toInt(), max = minAndMax[1].toInt())
    } catch (ex: Exception) {
        throw InvalidAgeRangeFormatException(rangeString)
    }
}
