package com.boclips.videos.service.domain.model.taxonomy

data class TaxonomyTree(
    val description: String,
    val code: String?,
    val children: Map<String, TaxonomyTree> = emptyMap()
)
