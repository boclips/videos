package com.boclips.contentpartner.service.domain.model.agerange

data class AgeRangeBuckets(
    val ageRanges: List<AgeRange>
) {
    val min: Int? by lazy { ageRanges.minBy { it.min }?.min }
    val max: Int? by lazy { ageRanges.maxBy { it.max ?: -1 }?.max }
    val ids = ageRanges.map { it.id }
}
