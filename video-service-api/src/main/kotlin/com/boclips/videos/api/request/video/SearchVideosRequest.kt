package com.boclips.videos.api.request.video

data class SearchVideosRequest(
    val subjects_set_manually: Boolean? = null,
    val page: Int? = null,
    val size: Int? = null,
    val channels_ids: Set<String> = emptySet()
)
