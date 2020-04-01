package com.boclips.contentpartner.service.domain.model.agerange

data class AgeRange(
    val id: AgeRangeId,
    val label: String,
    val min: Int,
    val max: Int?
)
