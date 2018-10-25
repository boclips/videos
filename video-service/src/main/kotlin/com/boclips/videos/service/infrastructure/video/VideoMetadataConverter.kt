package com.boclips.videos.service.infrastructure.video

import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.Video

object VideoMetadataConverter {
    fun convert(video: Video): VideoMetadata {
        return VideoMetadata(
                id = video.videoId.videoId,
                title = video.title,
                contentProvider = video.contentProvider,
                description = video.description,
                keywords = video.keywords
        )
    }
}