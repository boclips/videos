package com.boclips.videos.service.application.collection

import com.boclips.videos.service.presentation.Projection

data class CollectionFilter(
    val projection: Projection,
    val visibility: Boolean,
    val owner: String?,
    val pageNumber: Int,
    val pageSize: Int
) {
    fun isPublicCollections(): Boolean {
        return visibility
    }

    fun isMyCollections(): Boolean {
        return owner != null
    }
}