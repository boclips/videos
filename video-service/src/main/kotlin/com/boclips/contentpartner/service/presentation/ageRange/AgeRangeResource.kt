package com.boclips.contentpartner.service.presentation.ageRange

data class AgeRangeResource(
    val min: Int?,
    val max: Int?
) {
    fun getLabel() = min?.let { max?.let { "$min-$max" } ?: "$min+" } ?: ""
}
