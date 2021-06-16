package com.boclips.videos.service.domain.model.video.channel

import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import java.util.Locale

data class FallbackMetadata(
    val channel: Channel,
    val categories: Set<CategoryWithAncestors>?,
    val language: Locale?
)
