package com.boclips.videos.api.request.video

import com.fasterxml.jackson.annotation.JsonProperty


data class SearchVideosRequest(
    val subjects_set_manually: Boolean? = null
)
