package com.boclips.contentpartner.service.domain.model

data class EduAgeRange(
    val id: EduAgeRangeId,
    val label: String,
    val min: Int,
    val max: Int?
)

data class AgeRangeBuckets(
    val ageRanges: List<EduAgeRange>
) {
    val min: Int? by lazy { ageRanges.minBy { it.min }?.min }
    val max: Int? by lazy { ageRanges.maxBy { it.max ?: -1 }?.max }
    val ids = ageRanges.map { it.id }
}
