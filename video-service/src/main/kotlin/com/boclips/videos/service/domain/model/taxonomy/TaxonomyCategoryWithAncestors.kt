package com.boclips.videos.service.domain.model.taxonomy

data class TaxonomyCategoryWithAncestors(
        val codeValue: String,
        val description: String,
        val ancestors: Set<String>
)
