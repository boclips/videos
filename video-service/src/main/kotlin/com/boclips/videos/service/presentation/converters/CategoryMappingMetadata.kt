package com.boclips.videos.service.presentation.converters

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(
    CategoryMappingMetadata.CATEGORY_CODE,
    CategoryMappingMetadata.VIDEO_ID,
)
@JsonIgnoreProperties(ignoreUnknown = true)
class CategoryMappingMetadata {
    @JsonProperty(value = VIDEO_ID)
    var videoId: String? = null

    @JsonProperty(value = CATEGORY_CODE)
    var categoryCode: String? = null


    companion object {
        const val CATEGORY_CODE = "Thema code (where possible)"
        const val VIDEO_ID = "ID"
    }
}
