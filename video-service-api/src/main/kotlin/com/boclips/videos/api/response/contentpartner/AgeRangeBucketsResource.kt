package com.boclips.videos.api.response.contentpartner

data class AgeRangeBucketsResource(
    val min: Int?,
    val max: Int?,
    val ids: List<String>
) {
    fun getLabel() = min?.let { max?.let { "$min-$max" } ?: "$min+" } ?: ""
}
