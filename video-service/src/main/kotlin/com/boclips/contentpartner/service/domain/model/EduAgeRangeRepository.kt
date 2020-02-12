package com.boclips.contentpartner.service.domain.model

interface EduAgeRangeRepository {
    fun create(eduAgeRange: EduAgeRange): EduAgeRange
    fun findById(id: EduAgeRangeId): EduAgeRange?
    fun findAll(): Iterable<EduAgeRange>
}