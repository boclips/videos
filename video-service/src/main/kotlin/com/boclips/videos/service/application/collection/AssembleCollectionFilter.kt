package com.boclips.videos.service.application.collection

import com.boclips.videos.service.presentation.CollectionsController

class AssembleCollectionFilter {
    operator fun invoke(
        query: String? = null,
        subject: List<String>? = null,
        public: Boolean? = null,
        bookmarked: Boolean? = null,
        owner: String? = null,
        page: Int? = null,
        size: Int? = null
    ): CollectionFilter {
        return CollectionFilter(
            query = query ?: "",
            subjects = subject ?: emptyList(),
            visibility = determineVisibility(public, bookmarked),
            owner = owner,
            pageNumber = page ?: 0,
            pageSize = size ?: CollectionsController.COLLECTIONS_PAGE_SIZE
        )
    }

    private fun determineVisibility(
        public: Boolean?,
        bookmarked: Boolean?
        ): CollectionFilter.Visibility {
        return when {
            bookmarked == true -> CollectionFilter.Visibility.BOOKMARKED
            public == true -> CollectionFilter.Visibility.PUBLIC
            else -> CollectionFilter.Visibility.ALL
        }
    }
}
