package com.boclips.videos.service.infrastructure.video.mongo

import java.time.Instant

data class PlaybackDocument(
    val type: String,
    val id: String,
    val thumbnailUrl: List<String>?,
    val downloadUrl: String?,
    val hlsStreamUrl: String?,
    val dashStreamUrl: String?,
    val progressiveStreamUrl: String?,
    val lastVerified: Instant?,
    val duration: Int?
) {

    companion object {
        const val PLAYBACK_TYPE_KALTURA: String = "KALTURA"
        const val PLAYBACK_TYPE_YOUTUBE: String = "YOUTUBE"
    }

    fun isCompleteKalturaPlayback(): Boolean {
        return type == PLAYBACK_TYPE_KALTURA &&
            id.isNotEmpty() &&
            !thumbnailUrl.isNullOrEmpty() &&
            !hlsStreamUrl.isNullOrEmpty() &&
            !dashStreamUrl.isNullOrEmpty() &&
            !progressiveStreamUrl.isNullOrEmpty() &&
            !downloadUrl.isNullOrEmpty() &&
            duration != null
    }

    fun isCompleteYoutubePlayback(): Boolean {
        return type == PLAYBACK_TYPE_YOUTUBE &&
            id.isNotEmpty() &&
            !thumbnailUrl.isNullOrEmpty() &&
            duration != null
    }
}