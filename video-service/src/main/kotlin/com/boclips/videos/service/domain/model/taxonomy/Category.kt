package com.boclips.videos.service.domain.model.taxonomy

data class CategoryCode(val value: String)

data class Category(
    val parentCode: CategoryCode?,
    val description: String,
    val code: CategoryCode,
    val children: Map<CategoryCode, Category> = emptyMap()
)
