package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.Page
import com.boclips.videos.service.domain.model.PageRequest
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetPublicCollections(
    private val collectionService: CollectionService,
    private val collectionResourceFactory: CollectionResourceFactory
) {
    operator fun invoke(projection: CollectionsController.Projections, page: Int, size: Int): Page<CollectionResource> =
            collectionService.getPublic(PageRequest(page, size))
                    .let {
                        Page(
                                it.elements.map { collectionResourceFactory.buildCollectionResource(it, projection) },
                                it.pageInfo)
                    }

}