package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.domain.model.video.VideoId

object VideosCategoryMetadataConverter {
    fun convertCategories(metadata: List<CategoryMappingMetadata>): Map<VideoId, List<String>> {
        return metadata
            .groupBy { videoToCategory -> VideoId(videoToCategory.videoId) }
            .mapValues { videoToCategory ->
                videoToCategory.value
                    .map { metadata -> metadata.categoryCode }
                    .filter { it.isNotBlank() }
            }
    }

    fun convertTags(metadata: List<CategoryMappingMetadata>): Map<VideoId, List<String>> {
        return metadata
            .groupBy { videoToTag -> VideoId(videoToTag.videoId) }
            .mapValues { videoToTag ->
                videoToTag.value.map { metadata -> metadata.tag }
                    .filter { it.isNotBlank() }
            }
    }
}
