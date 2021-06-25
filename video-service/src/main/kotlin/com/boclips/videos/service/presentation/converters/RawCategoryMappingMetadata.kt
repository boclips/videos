package com.boclips.videos.service.presentation.converters

data class RawCategoryMappingMetadata(
    val videoId: String?,
    val categoryCode: String?
) {
    fun validated(index: Int) =
        videoId?.let { CategoryMappingMetadata(it, categoryCode!!, index + 2) } //  0 index + header in csv
}

data class CategoryMappingMetadata(
    val videoId: String,
    val categoryCode: String,
    val index: Int
)
