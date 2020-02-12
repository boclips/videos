package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.EduAgeRange
import com.boclips.contentpartner.service.presentation.EduAgeRangeLinkBuilder
import com.boclips.videos.api.response.contentpartner.EduAgeRangeResource

class EduAgeRangeResourceConverter(
    private val eduAgeRangeLinkBuilder: EduAgeRangeLinkBuilder
) {
    fun convert(eduAgeRange: EduAgeRange): EduAgeRangeResource {
        return EduAgeRangeResource(
            id = eduAgeRange.id.value,
            label = eduAgeRange.label,
            min = eduAgeRange.min,
            max = eduAgeRange.max,
            _links = listOf(eduAgeRangeLinkBuilder.self(eduAgeRange.id.value)).map { it.rel to it }.toMap()
        )
    }
}