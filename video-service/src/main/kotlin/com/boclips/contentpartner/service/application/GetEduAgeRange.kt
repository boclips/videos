package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.exceptions.EduAgeRangeNotFoundException
import com.boclips.contentpartner.service.domain.model.EduAgeRange
import com.boclips.contentpartner.service.domain.model.EduAgeRangeId
import com.boclips.contentpartner.service.domain.model.EduAgeRangeRepository

class GetEduAgeRange(private val eduAgeRangeRepository: EduAgeRangeRepository) {
    operator fun invoke(id: EduAgeRangeId): EduAgeRange {
        return eduAgeRangeRepository.findById(id) ?: throw EduAgeRangeNotFoundException(id.value)
    }
}