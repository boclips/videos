package com.boclips.videos.api.request.collection

import com.boclips.videos.api.request.Projection

class CollectionFilterRequest(
    val query: String? = null,
    val discoverable: Boolean? = null,
    val ignore_discoverable: Boolean? = false,
    val bookmarked: Boolean? = null,
    val promoted: Boolean? = null,
    val has_lesson_plans: Boolean? = null,
    val owner: String? = null,
    val subject: String? = null,
    val page: Int? = null,
    val size: Int? = null,
    val age_range_min: Int? = null,
    val age_range_max: Int? = null,
    val age_range: String? = null,
    val sort_by: String? = null,
    val resource_types: String? = null,
    val projection: Projection? = Projection.list
) {
    fun getAgeRanges(): List<String> {
        return age_range?.split(",") ?: emptyList()
    }

    fun getResourceTypes(): Set<String> {
        return resource_types?.split(",")?.toSet() ?: emptySet()
    }

    fun getSortKeys(): List<CollectionSortKey> {
        return sort_by?.split(",")?.map { CollectionSortKey.valueOf(it) } ?: emptyList()
    }
}

enum class CollectionSortKey {
    TITLE,
    UPDATED_AT,
    IS_DEFAULT,
    HAS_ATTACHMENT
}
