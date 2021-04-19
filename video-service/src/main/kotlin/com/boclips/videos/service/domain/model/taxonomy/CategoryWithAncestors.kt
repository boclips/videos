package com.boclips.videos.service.domain.model.taxonomy

data class CategoryWithAncestors(
    val codeValue: CategoryCode,
    val description: String,
    val ancestors: Set<CategoryCode> = emptySet()
)
