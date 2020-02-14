package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.domain.model.AgeRangeId
import com.boclips.contentpartner.service.domain.model.AgeRangeRepository
import com.boclips.videos.api.request.contentpartner.AgeRangeRequest

class CreateAgeRange(private val ageRangeRepository: AgeRangeRepository) {
    operator fun invoke(createAgeRange: AgeRangeRequest): AgeRange {
        return ageRangeRepository.create(
            AgeRange(
                id = AgeRangeId(createAgeRange.id),
                label = createAgeRange.label?.let { it }
                    ?: throw IllegalStateException("Cannot create an age range with a label"),
                min = createAgeRange.min?.let { it }
                    ?: throw IllegalStateException("Cannot create an age range with non minimum"),
                max = createAgeRange.max?.let { it }
            )
        )
    }
}
