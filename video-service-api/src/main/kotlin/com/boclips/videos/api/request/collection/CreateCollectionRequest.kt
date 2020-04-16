package com.boclips.videos.api.request.collection

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import javax.validation.constraints.NotBlank

data class CreateCollectionRequest(
    @field:NotBlank(message = "Title is required")
    val title: String?,
    val description: String? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val videos: List<String> = emptyList(),
    val public: Boolean? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val subjects: Set<String> = emptySet()
)
