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
    val subject: String? = null,
    val age_range_min: Int? = null,
    val age_range_max: Int? = null,
    val age_range: List<String>? = null
)

enum class CollectionSortKey {
    TITLE
}
