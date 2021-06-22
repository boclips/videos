package com.boclips.videos.service.domain.model.video

data class Transcript(
    val content: String? = null,
    val isHumanGenerated: Boolean? = false,
    val isRequested: Boolean? = false
)
