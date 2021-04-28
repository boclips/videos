package com.boclips.contentpartner.service.infrastructure.channel

import com.boclips.videos.service.infrastructure.taxonomy.CategoryWithAncestorsDocument

data class TaxonomyDocument(
    val categories: Set<CategoryWithAncestorsDocument>? = null,
    val requiresVideoLevelTagging: Boolean? = null
)
