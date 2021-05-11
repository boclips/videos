package com.boclips.search.service.domain.channels.model

data class Taxonomy(
    val videoLevelTagging: Boolean,
    val categories: Set<CategoryCode>? = null
)

data class CategoryCode(val value: String)
