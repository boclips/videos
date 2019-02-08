package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.ContentEnrichers
import com.boclips.videos.service.domain.model.asset.VideoAsset

object VideoMetadataConverter {
    fun convert(video: VideoAsset): VideoMetadata {
        return VideoMetadata(
            id = video.assetId.value,
            title = video.title,
            description = video.description,
            contentProvider = video.contentPartnerId,
            keywords = video.keywords,
            tags = tagsFrom(video)
        )
    }

    private fun tagsFrom(video: VideoAsset): List<String> {
        return listOf(
            Pair("classroom", ContentEnrichers.isClassroom(video)),
            Pair("news", ContentEnrichers.isNews(video))
        ).fold(emptyList()) { acc, pair -> if (pair.second) acc.plus(pair.first) else acc }
    }
}