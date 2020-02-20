package com.boclips.videos.api.response.contentpartner

data class ContentPartnerPedagogyResource(
    val isTranscriptProvided: Boolean?,
    val educationalResources: String?,
    val curriculumAligned: String?,
    val bestForTags: List<String>?,
    val subjects: List<String>?,
    val ageRanges: AgeRangeBucketsResource?
)