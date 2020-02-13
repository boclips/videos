package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.EduAgeRange
import com.boclips.contentpartner.service.domain.model.EduAgeRangeId

object EduAgeRangeDocumentConverter {
    fun toEduAgeRangeDocument(eduAgeRange: EduAgeRange): EduAgeRangeDocument {
        return EduAgeRangeDocument(
            id = eduAgeRange.id.value,
            label = eduAgeRange.label,
            min = eduAgeRange.min,
            max = eduAgeRange.max
        )
    }

    fun toEduAgeRange(document: EduAgeRangeDocument): EduAgeRange {
        return EduAgeRange(
            id = EduAgeRangeId(document.id),
            label = document.label,
            min = document.min,
            max = document.max
        )
    }
}
