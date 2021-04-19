package com.boclips.videos.service.domain.model.taxonomy

data class TaxonomyCategoryWithAncestors(
    val codeValue: CategoryCode,
    val description: String,
    val ancestors: List<CategoryCode> = emptyList()
)
