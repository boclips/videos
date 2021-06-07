package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.domain.model.video.VideoId

object VideosCategoryMetadataConverter {
    fun convert(metadata: List<CategoryMappingMetadata>): Map<VideoId, List<String>> {
        return metadata
            .groupBy { videoToCategory -> VideoId(videoToCategory.videoId) }
            .mapValues { videoToCategory ->
                videoToCategory.value
                    .map { metadata -> metadata.categoryCode }
                    .filter { it.isNotBlank() }
            }
    }
}
