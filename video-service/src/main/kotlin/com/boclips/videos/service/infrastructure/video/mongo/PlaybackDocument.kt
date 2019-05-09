package com.boclips.videos.service.infrastructure.video.mongo

import java.time.Instant

data class PlaybackDocument(
    val type: String,
    val id: String,
    val thumbnailUrl: List<String>?,
    val downloadUrl: String?,
    val hdsStreamUrl: String?,
    val dashStreamUrl: String?,
    val progressiveStreamUrl: String?,
    val lastVerified: Instant?,
    val duration: Int?
)