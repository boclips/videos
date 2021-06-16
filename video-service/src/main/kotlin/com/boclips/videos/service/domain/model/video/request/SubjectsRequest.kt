package com.boclips.videos.service.domain.model.video.request

data class SubjectsRequest(
    val ids: Set<String> = emptySet(),
    val setManually: Boolean? = null
)
