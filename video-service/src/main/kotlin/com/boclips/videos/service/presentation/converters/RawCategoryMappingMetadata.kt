package com.boclips.videos.service.presentation.converters

data class RawCategoryMappingMetadata(
    val videoId: String?,
    val categoryCode: String?
) {
    private val headerOffset = 2

    fun validated(index: Int) =
        videoId?.let {
            val indexWithIncludedHeader = index + headerOffset
            CategoryMappingMetadata(it, categoryCode!!, indexWithIncludedHeader)
        }
}

data class CategoryMappingMetadata(
    val videoId: String,
    val categoryCode: String,
    val index: Int
)
