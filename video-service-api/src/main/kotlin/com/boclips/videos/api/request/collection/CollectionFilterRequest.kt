package com.boclips.videos.api.request.collection

import com.boclips.videos.api.request.Projection

class CollectionFilterRequest(
    val query: String? = null,
    val discoverable: Boolean? = null,
    val bookmarked: Boolean? = null,
    val promoted: Boolean? = null,
    val has_lesson_plans: Boolean? = null,
    val owner: String? = null,
    val subject: String? = null,
    val page: Int? = null,
    val size: Int? = null,
    val age_range_min: Int? = null,
    val age_range_max: Int? = null,
    val age_range: List<String>? = null,
    val sort_by: List<String>? = null,
    val resource_types: Set<String>? = null,
    val projection: Projection? = Projection.list
) {
    fun getAgeRanges(): List<String> {
        return age_range ?: emptyList()
    }

    fun getSortKeys(): List<CollectionSortKey> {
        return sort_by?.map { CollectionSortKey.valueOf(it) } ?: emptyList()
    }
}

enum class CollectionSortKey {
    TITLE,
    UPDATED_AT,
    IS_DEFAULT,
    HAS_ATTACHMENT
}
