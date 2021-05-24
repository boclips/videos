package com.boclips.videos.service.presentation.converters

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonIgnoreProperties(ignoreUnknown = true)
data class CategoryMappingMetadata(
    @JsonProperty("Category Code")
    val categoryCode: String?,
    @JsonProperty("ID")
    val videoId: String?
)
