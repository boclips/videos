package com.boclips.videos.service.infrastructure.video

import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.asset.VideoAsset

object VideoMetadataConverter {
    fun convert(video: VideoAsset): VideoMetadata {
        return VideoMetadata(
                id = video.assetId.value,
                title = video.title,
                contentProvider = video.contentProvider,
                description = video.description,
                keywords = video.keywords
        )
    }
}