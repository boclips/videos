package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.Video
import java.time.Instant

data class Collection(
    val id: CollectionId,
    val owner: UserId,
    val title: String,
    val videos: List<Video>,
    val updatedAt: Instant
) {
    companion object {
        const val DEFAULT_TITLE = "My Videos"
    }
}