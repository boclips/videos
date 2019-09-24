package com.boclips.videos.service.presentation.collections

data class CreateCollectionRequest(
    val title: String?,
    val description: String? = null,
    val videos: List<String> = emptyList(),
    val public: Boolean? = null
)