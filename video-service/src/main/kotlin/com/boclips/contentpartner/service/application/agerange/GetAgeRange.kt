package com.boclips.contentpartner.service.application.agerange

import com.boclips.contentpartner.service.application.exceptions.AgeRangeNotFoundException
import com.boclips.contentpartner.service.domain.model.agerange.AgeRange
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository

class GetAgeRange(private val ageRangeRepository: AgeRangeRepository) {
    operator fun invoke(id: AgeRangeId): AgeRange {
        return ageRangeRepository.findById(id) ?: throw AgeRangeNotFoundException(id.value)
    }
}
