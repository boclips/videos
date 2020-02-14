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
    private val overallAgeRange = AgeRange.bounded(
        min = ageRanges.minBy { it.min }?.min,
        max = ageRanges.maxBy { it.max ?: -1 }?.max
    )

    val min = overallAgeRange.min
    val max = overallAgeRange.max
    val ids = ageRanges.map { it.id }
}
