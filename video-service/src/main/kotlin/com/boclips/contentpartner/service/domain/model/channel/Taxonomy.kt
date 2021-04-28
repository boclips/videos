package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors

sealed class Taxonomy {
    object VideoLevelTagging : Taxonomy()
    data class ChannelLevelTagging(val categories: Set<CategoryWithAncestors>): Taxonomy()
}
