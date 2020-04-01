package com.boclips.contentpartner.service.domain.model.agerange

import com.boclips.contentpartner.service.domain.model.agerange.AgeRange
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId

interface AgeRangeRepository {
    fun create(ageRange: AgeRange): AgeRange
    fun findById(id: AgeRangeId): AgeRange?
    fun findAll(): Iterable<AgeRange>
}
