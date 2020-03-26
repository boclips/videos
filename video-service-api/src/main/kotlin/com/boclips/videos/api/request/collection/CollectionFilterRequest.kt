package com.boclips.videos.api.request.collection

import com.boclips.videos.api.request.Projection

class CollectionFilterRequest(
    val query: String? = null,
    val public: Boolean? = null,
    val bookmarked: Boolean? = null,
    val owner: String? = null,
    val page: Int? = null,
    val size: Int? = null,
    val sort_by: CollectionSortKey? = null,
    val projection: Projection? = Projection.list,
    val has_lesson_plans: Boolean? = null,
    val promoted: Boolean? = null,
    val subject: String? = null,
    val age_range_min: Int? = null,
    val age_range_max: Int? = null,
    val age_range: String? = null
) {
    fun getAgeRanges(): List<String> {
        return age_range?.split(",") ?: emptyList()
    }
}

enum class CollectionSortKey {
    TITLE,
    UPDATED_AT
}
