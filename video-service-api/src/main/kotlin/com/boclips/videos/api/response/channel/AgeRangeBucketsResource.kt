package com.boclips.videos.api.response.channel

data class AgeRangeBucketsResource(
    val min: Int?,
    val max: Int?,
    val ids: List<String>
) {
    fun getLabel() = min?.let { max?.let { "$min-$max" } ?: "$min+" } ?: ""
}
