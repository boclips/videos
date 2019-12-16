package com.boclips.videos.service.domain.model.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.videos.service.domain.model.UserId

data class CollectionFilter(
    val query: String? = null,
    val queriedVisibilities: CollectionVisibilityQuery = CollectionVisibilityQuery.All,
    val onlyBookmarked: Boolean = false,
    val owner: UserId? = null,
    val pageNumber: Int,
    val pageSize: Int,
    val subjects: List<String> = emptyList()
)
