package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetPublicCollections(
    private val collectionService: CollectionService,
    private val collectionResourceFactory: CollectionResourceFactory
) {
    operator fun invoke(projection: CollectionsController.Projections): List<CollectionResource> =
        collectionService.getPublic()
            .map { collectionResourceFactory.buildCollectionResource(it, projection) }
}