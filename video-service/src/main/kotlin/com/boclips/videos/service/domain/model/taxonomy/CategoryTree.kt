package com.boclips.videos.service.domain.model.taxonomy

data class CategoryTree(
    val codeValue: CategoryCode,
    val description: String,
    val parent: CategoryTree? = null
)
