package com.boclips.videos.api.request.collection

import javax.validation.constraints.NotBlank

data class CreateCollectionRequest(
    @field:NotBlank(message = "Title is required")
    val title: String?,
    val description: String? = null,
    val videos: List<String> = emptyList(),
    val public: Boolean? = null,
    val subjects: Set<String> = emptySet()
)
