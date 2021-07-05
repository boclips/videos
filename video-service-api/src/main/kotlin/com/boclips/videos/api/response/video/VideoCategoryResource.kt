package com.boclips.videos.api.response.video

data class VideoCategoryResource(
    val code: String,
    val value: String,
    val parent: VideoCategoryResource? = null
)
