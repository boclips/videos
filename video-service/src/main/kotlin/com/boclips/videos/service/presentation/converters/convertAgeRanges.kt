package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.application.exceptions.InvalidAgeRangeFormatException
import com.boclips.videos.service.domain.model.AgeRange

fun convertAgeRanges(rangeString: String): AgeRange {
    try {
        val minAndMax = rangeString.split('-')
        return AgeRange.of(minAndMax[0].toInt(), minAndMax[1].toInt())
    } catch (ex: Exception) {
        throw InvalidAgeRangeFormatException(rangeString)
    }
}
