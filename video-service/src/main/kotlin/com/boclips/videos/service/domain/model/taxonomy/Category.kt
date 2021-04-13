package com.boclips.videos.service.domain.model.taxonomy

data class Category(
    val description: String,
    val code: String?,
    val children: Map<String, Category> = emptyMap()
)
