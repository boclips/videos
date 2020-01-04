package com.boclips.videos.api.request.video

data class UpdateVideoRequest(
    val title: String? = null,
    val description: String? = null,
    val promoted: Boolean? = null,
    val subjectIds: List<String>? = null
)
