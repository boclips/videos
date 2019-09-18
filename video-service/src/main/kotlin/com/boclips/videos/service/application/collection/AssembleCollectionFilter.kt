package com.boclips.videos.service.application.collection

import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.Projection

class AssembleCollectionFilter {
    operator fun invoke(
        query: String? = null,
        subject: List<String>? = null,
        public: Boolean? = null,
        bookmarked: Boolean? = null,
        owner: String? = null,
        projection: Projection? = null,
        page: Int? = null,
        size: Int? = null
    ): CollectionFilter {
        return CollectionFilter(
            query = query ?: "",
            subjects = subject ?: emptyList(),
            visibility = determineVisibility(public, bookmarked, owner),
            owner = owner,
            pageNumber = page ?: 0,
            pageSize = size ?: CollectionsController.COLLECTIONS_PAGE_SIZE,
            projection = projection ?: Projection.list
        )
    }

    private fun determineVisibility(
        public: Boolean?,
        bookmarked: Boolean?,
        owner: String?
    ): CollectionFilter.Visibility {
        return when {
            bookmarked == true -> CollectionFilter.Visibility.BOOKMARKED
            public == true -> CollectionFilter.Visibility.PUBLIC
            owner != null -> CollectionFilter.Visibility.PRIVATE
            else -> CollectionFilter.Visibility.ALL
        }
    }
}