package com.boclips.videos.service.presentation.converters

data class RawCategoryMappingMetadata(
    val videoId: String?,
    val categoryCode: String?,
    val tag: String?
) {
    private val headerOffset = 2

    fun validated(index: Int) =
        videoId?.let {
            val indexWithIncludedHeader = index + headerOffset
            val tagOrEmpty = tag ?: ""
            CategoryMappingMetadata(it, categoryCode!!, tagOrEmpty, indexWithIncludedHeader)
        }
}

data class CategoryMappingMetadata(
    val videoId: String,
    val categoryCode: String,
    val tag: String,
    val index: Int
)
