package com.boclips.videos.service.presentation.converters

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(
    CategoryMappingMetadata.VIDEO_ID,
    CategoryMappingMetadata.CATEGORY_CODE,
)

class CategoryMappingMetadata {
    @JsonProperty(value = VIDEO_ID)
    var videoId: String? = null

    @JsonProperty(value = CATEGORY_CODE)
    var categoryCode: String? = null


    companion object {
        const val VIDEO_ID = "ID"
        const val CATEGORY_CODE = "Thema code (where possible)"
    }
}
