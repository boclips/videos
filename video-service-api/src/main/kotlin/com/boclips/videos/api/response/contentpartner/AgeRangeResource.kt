package com.boclips.videos.api.response.contentpartner

data class AgeRangeResource(
    val min: Int?,
    val max: Int?
) {
    fun getLabel() = min?.let { max?.let { "$min-$max" } ?: "$min+" } ?: ""
}
