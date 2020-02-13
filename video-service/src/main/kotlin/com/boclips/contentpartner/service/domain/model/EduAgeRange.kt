package com.boclips.contentpartner.service.domain.model

data class EduAgeRange(
    val id: EduAgeRangeId,
    val label: String,
    val min: Int,
    val max: Int?
)
