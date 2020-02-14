package com.boclips.contentpartner.service.domain.model

interface AgeRangeRepository {
    fun create(ageRange: AgeRange): AgeRange
    fun findById(id: AgeRangeId): AgeRange?
    fun findAll(): Iterable<AgeRange>
}
