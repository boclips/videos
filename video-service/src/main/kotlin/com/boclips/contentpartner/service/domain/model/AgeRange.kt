package com.boclips.contentpartner.service.domain.model

data class AgeRange(
    val id: AgeRangeId,
    val label: String,
    val min: Int,
    val max: Int?
)
