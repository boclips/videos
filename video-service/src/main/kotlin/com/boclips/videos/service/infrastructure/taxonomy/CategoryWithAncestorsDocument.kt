package com.boclips.videos.service.infrastructure.taxonomy

data class CategoryWithAncestorsDocument(
    val codeValue: String,
    val description: String,
    val ancestors: Set<String>
)
