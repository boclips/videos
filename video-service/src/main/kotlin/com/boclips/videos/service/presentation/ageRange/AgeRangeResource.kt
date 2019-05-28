package com.boclips.videos.service.presentation.ageRange

data class AgeRangeResource(
    val min: Int,
    val max: Int?
) {
    fun getLabel() = max?.let { "$min-$max" } ?: "$min+"
}
