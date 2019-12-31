package com.boclips.videos.api.request.video

data class AdminSearchRequest(
    val ids: List<String>?,
    val contentPartnerId: String?
)