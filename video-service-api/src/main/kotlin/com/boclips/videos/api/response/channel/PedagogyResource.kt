package com.boclips.videos.api.response.channel

data class PedagogyResource(
    val isTranscriptProvided: Boolean?,
    val educationalResources: String?,
    val curriculumAligned: String?,
    val bestForTags: List<String>?,
    val subjects: List<String>?,
    val ageRanges: AgeRangeBucketsResource?
)
