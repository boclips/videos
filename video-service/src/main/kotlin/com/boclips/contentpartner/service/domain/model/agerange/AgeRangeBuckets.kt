package com.boclips.contentpartner.service.domain.model.agerange

data class AgeRangeBuckets(
    val ageRanges: List<AgeRange>
) {
    val min: Int? by lazy { ageRanges.minByOrNull { it.min }?.min }
    val max: Int? by lazy { ageRanges.maxByOrNull { it.max ?: -1 }?.max }
    val ids = ageRanges.map { it.id }
}
