package com.boclips.search.service.domain.channels.model

data class Taxonomy(
    val videoLevelTagging: Boolean,
    val categories: Set<CategoryCode>? = null
) : Comparable<Taxonomy> {
    override fun compareTo(other: Taxonomy): Int {
        return 0
    }
}

data class CategoryCode(val value: String)
