package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.EduAgeRange
import com.boclips.contentpartner.service.domain.model.EduAgeRangeId
import com.boclips.contentpartner.service.domain.model.EduAgeRangeRepository
import com.boclips.videos.api.request.contentpartner.EduAgeRangeRequest

class CreateEduAgeRange(private val eduAgeRangeRepository: EduAgeRangeRepository) {
    operator fun invoke(createEduAgeRange: EduAgeRangeRequest): EduAgeRange {
        return eduAgeRangeRepository.create(
            EduAgeRange(
                id = EduAgeRangeId(createEduAgeRange.id),
                label = createEduAgeRange.label?.let { it }
                    ?: throw IllegalStateException("Cannot create an age range with a label"),
                min = createEduAgeRange.min?.let { it }
                    ?: throw IllegalStateException("Cannot create an age range with non minimum"),
                max = createEduAgeRange.max?.let { it }
            )
        )
    }
}
