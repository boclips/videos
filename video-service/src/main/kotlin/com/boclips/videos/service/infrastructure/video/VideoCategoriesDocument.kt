package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.infrastructure.taxonomy.CategoryWithAncestorsDocument

data class VideoCategoriesDocument(
    val channel: Set<CategoryWithAncestorsDocument>?,
    val manual: Set<CategoryWithAncestorsDocument>?,
)
