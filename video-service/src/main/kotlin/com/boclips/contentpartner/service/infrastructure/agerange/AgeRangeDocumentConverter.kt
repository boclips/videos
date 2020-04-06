package com.boclips.contentpartner.service.infrastructure.agerange

import com.boclips.contentpartner.service.domain.model.agerange.AgeRange
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId

object AgeRangeDocumentConverter {
    fun toAgeRangeDocument(ageRange: AgeRange): AgeRangeDocument {
        return AgeRangeDocument(
            id = ageRange.id.value,
            label = ageRange.label,
            min = ageRange.min,
            max = ageRange.max
        )
    }

    fun toAgeRange(document: AgeRangeDocument): AgeRange {
        return AgeRange(
            id = AgeRangeId(document.id),
            label = document.label,
            min = document.min,
            max = document.max
        )
    }
}
