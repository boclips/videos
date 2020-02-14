package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.presentation.AgeRangeLinkBuilder
import com.boclips.videos.api.response.contentpartner.AgeRangeResource

class AgeRangeResourceConverter(
    private val ageRangeLinkBuilder: AgeRangeLinkBuilder
) {
    fun convert(ageRange: AgeRange): AgeRangeResource {
        return AgeRangeResource(
            id = ageRange.id.value,
            label = ageRange.label,
            min = ageRange.min,
            max = ageRange.max,
            _links = listOf(ageRangeLinkBuilder.self(ageRange.id.value)).map { it.rel to it }.toMap()
        )
    }
}
