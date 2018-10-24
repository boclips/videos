package com.boclips.videos.service.infrastructure.video

import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.Video

object VideoMetadataConverter {
    fun convert(video: Video): VideoMetadata {
        val id = video.videoId.videoId.toLong()
        val title = video.title
        val description = video.description
        val keywords = video.keywords
        return VideoMetadata(id = id.toString(), title = title, description = description, keywords = keywords)
    }
}