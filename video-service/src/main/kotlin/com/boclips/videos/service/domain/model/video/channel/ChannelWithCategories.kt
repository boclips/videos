package com.boclips.videos.service.domain.model.video.channel

import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors

data class ChannelWithCategories(
    val channel: Channel,
    val categories: Set<CategoryWithAncestors>?,
)
