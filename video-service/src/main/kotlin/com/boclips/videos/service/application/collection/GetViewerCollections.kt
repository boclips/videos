package com.boclips.videos.service.application.collection

import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetViewerCollections(
    private val collectionRepository: CollectionRepository,
    private val collectionResourceFactory: CollectionResourceFactory
) {
    operator fun invoke(viewerId: UserId): Page<CollectionResource> {
        val collections = collectionRepository.getByViewer(viewerId, PageRequest(0, 100))
        return assemblePage(collections.pageInfo, collections.elements)
    }

    private fun assemblePage(
        pageInfo: PageInfo,
        collections: Iterable<Collection>
    ): Page<CollectionResource> {
        return Page(
            collections.map {
                collectionResourceFactory.buildCollectionResource(
                    it,
                    Projection.details
                )
            },
            pageInfo
        )
    }
}