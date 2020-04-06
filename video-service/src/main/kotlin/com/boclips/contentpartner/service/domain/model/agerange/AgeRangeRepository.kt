package com.boclips.contentpartner.service.domain.model.agerange

interface AgeRangeRepository {
    fun create(ageRange: AgeRange): AgeRange
    fun findById(id: AgeRangeId): AgeRange?
    fun findAll(): Iterable<AgeRange>
}
