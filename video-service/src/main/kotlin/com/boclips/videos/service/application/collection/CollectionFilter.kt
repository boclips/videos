package com.boclips.videos.service.application.collection

import com.boclips.videos.service.presentation.Projection

data class CollectionFilter(
    val query: String? = null,
    val projection: Projection,
    val visibility: Visibility,
    val owner: String? = null,
    val pageNumber: Int,
    val pageSize: Int,
    val subjects: List<String> = emptyList()
) {
    enum class Visibility {
        PUBLIC, PRIVATE, BOOKMARKED
    }

    fun isPublicCollections(): Boolean {
        return visibility == Visibility.PUBLIC
    }

    fun isMyCollections(): Boolean {
        return owner != null
    }
}