package com.boclips.videos.service.presentation.converters

data class RawCategoryMappingMetadata(
    val videoId: String?,
    val categoryCode: String?
) {
    fun validated() = CategoryMappingMetadata(videoId!!, categoryCode!!)
}

data class CategoryMappingMetadata(
    val videoId: String,
    val categoryCode: String
)
