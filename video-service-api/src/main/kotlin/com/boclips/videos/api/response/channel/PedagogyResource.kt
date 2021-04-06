package com.boclips.videos.api.response.channel

data class PedagogyResource(
    val bestForTags: List<String>?,
    val subjects: List<String>?,
    val ageRanges: AgeRangeBucketsResource?
)
