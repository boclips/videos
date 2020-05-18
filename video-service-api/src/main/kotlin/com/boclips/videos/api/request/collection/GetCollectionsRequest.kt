package com.boclips.videos.api.request.collection

import com.boclips.videos.api.request.Projection

data class GetCollectionsRequest(
    val query: String? = null,
    val public: Boolean? = null,
    val bookmarked: Boolean? = null,
    val owner: String? = null,
    val page: Int? = null,
    val size: Int? = null,
    val projection: Projection? = Projection.list,
    val hasLessonPlans: Boolean? = null,
    val subject: String? = null
) {
    val subjects = subject?.split(",") ?: emptyList()
}
