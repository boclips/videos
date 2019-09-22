package com.boclips.videos.service.application.collection

data class CollectionFilter(
    val query: String? = null,
    val visibility: Visibility,
    val owner: String? = null,
    val pageNumber: Int,
    val pageSize: Int,
    val subjects: List<String> = emptyList()
) {
    enum class Visibility {
        PUBLIC, PRIVATE, BOOKMARKED, ALL
    }
}