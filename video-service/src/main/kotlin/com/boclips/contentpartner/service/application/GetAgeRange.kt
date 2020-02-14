package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.exceptions.AgeRangeNotFoundException
import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.domain.model.AgeRangeId
import com.boclips.contentpartner.service.domain.model.AgeRangeRepository

class GetAgeRange(private val ageRangeRepository: AgeRangeRepository) {
    operator fun invoke(id: AgeRangeId): AgeRange {
        return ageRangeRepository.findById(id) ?: throw AgeRangeNotFoundException(id.value)
    }
}
