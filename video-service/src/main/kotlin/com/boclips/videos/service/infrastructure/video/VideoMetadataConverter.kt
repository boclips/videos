package com.boclips.videos.service.infrastructure.video

import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.VideoDetails

object VideoMetadataConverter {
    fun convert(video: VideoDetails): VideoMetadata {
        return VideoMetadata(
                id = video.videoId.value,
                title = video.title,
                contentProvider = video.contentProvider,
                description = video.description,
                keywords = video.keywords
        )
    }
}