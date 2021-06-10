package com.boclips.search.service.domain.channels.model

data class Taxonomy(
    val videoLevelTagging: Boolean,
    val categories: Set<CategoryCode>? = null,
    val categoriesWithAncestors: Set<CategoryCode>? = null
) : Comparable<Taxonomy> {
    override fun compareTo(other: Taxonomy): Int {
        return this.getSortPriority().compareTo(other.getSortPriority())
    }

    private fun getSortPriority(): String {
        return if (this.videoLevelTagging) {
            "1"
        } else if (this.categories == null || this.categories.isEmpty()) {
            "0"
        } else {
            this.categories.map { it.value }.sorted().first()
        }
    }
}

data class CategoryCode(val value: String)
