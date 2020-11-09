package com.boclips.videos.api.request.admin

data class VideosForContentPackageParams(
    val size: Int = 10000,
    val cursor: String? = null
)
