package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.Page
import com.boclips.videos.service.domain.model.PageInfo
import com.boclips.videos.service.domain.model.PageRequest
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.Projections
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import getCurrentUserId

class GetCollections(
    private val collectionService: CollectionService,
    private val collectionResourceFactory: CollectionResourceFactory
) {
    operator fun invoke(
        projection: Projections,
        public: Boolean,
        owner: String?,
        page: Int,
        size: Int
    ): Page<CollectionResource> {
        if (public) {
            return collectionService.getPublic(PageRequest(page, size))
                .let { collection ->
                    Page(
                        collection.elements.map { collectionResourceFactory.buildCollectionResource(it, projection) },
                        collection.pageInfo
                    )
                }
        } else {
            if (owner == null || owner != getCurrentUserId().value) {
                return Page(elements = emptyList(), pageInfo = PageInfo(hasMoreElements = false))
            }

            return collectionService
                .getByOwner(getCurrentUserId(), PageRequest(page, size))
                .let { collection ->
                    Page(
                        collection.elements.map { collectionResourceFactory.buildCollectionResource(it, projection) },
                        collection.pageInfo
                    )
                }
        }
    }
}