package com.boclips.contentpartner.service.domain.model

data class PedagogyInformation(
    val isTranscriptProvided: Boolean? = null,
    val educationalResources: String? = null,
    val curriculumAligned: String? = null,
    val bestForTags: List<String>? = null,
    val subjects: List<String>? = null,
    val ageRangeBuckets: AgeRangeBuckets? = null
)