package com.boclips.videos.service.infrastructure.video

import java.time.Instant

data class VideoAssetDocument(
    val id: String?,
    val sizeKb: Int?,
    val width: Int?,
    val height: Int?,
    val bitrateKbps: Int?
)

data class PlaybackDocument(
    val type: String,
    val id: String,
    val entryId: String?,
    val thumbnailUrl: List<String>?,
    val thumbnailSecond: Int?,
    val customThumbnail: Boolean?,
    val downloadUrl: String?,
    val lastVerified: Instant?,
    val duration: Int?,
    val assets: List<VideoAssetDocument>?,
    val originalWidth: Int?,
    val originalHeight: Int?
) {

    companion object {
        const val PLAYBACK_TYPE_KALTURA: String = "KALTURA"
        const val PLAYBACK_TYPE_YOUTUBE: String = "YOUTUBE"
    }

    fun isCompleteKalturaPlayback(): Boolean {
        return type == PLAYBACK_TYPE_KALTURA &&
            id.isNotEmpty() &&
            !entryId.isNullOrEmpty() &&
            !downloadUrl.isNullOrEmpty() &&
            duration != null
    }

    fun isCompleteYoutubePlayback(): Boolean {
        return type == PLAYBACK_TYPE_YOUTUBE &&
            id.isNotEmpty() &&
            duration != null &&
            !thumbnailUrl.isNullOrEmpty()
    }
}
