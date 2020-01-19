package com.boclips.videos.api.request.collection

import com.boclips.videos.api.request.Projection

class CollectionFilterRequest(
    val query: String? = null,
    val public: Boolean? = null,
    val bookmarked: Boolean? = null,
    val owner: String? = null,
    val page: Int? = null,
    val size: Int? = null,
    val projection: Projection? = Projection.list,
    val has_lesson_plans: Boolean? = null,
    val subject: String? = null
)
