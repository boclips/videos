package com.boclips.videos.service.presentation.converters.customMetadata

data class RawCustomMappingMetadata(
    val videoId: String?,
    val topic: String?,
    val topicId: String?
) {
    private val headerOffset = 2

    fun validated(index: Int): CustomMappingMetadata? {
        return videoId?.let {
            val indexWithIncludedHeader = index + headerOffset
            CustomMappingMetadata(videoId = it, topic = topic!!, topicId = topicId!!, index = indexWithIncludedHeader)
        }
    }
}

data class CustomMappingMetadata(
    val videoId: String,
    val topic: String,
    val topicId: String,
    val index: Int
)
