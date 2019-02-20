package com.boclips.videos.service.presentation.collections

data class CreateCollectionRequest(
    val title: String?,
    val videos: List<String> = emptyList()
)