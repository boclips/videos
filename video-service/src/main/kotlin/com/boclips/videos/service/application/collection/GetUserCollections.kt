package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.PageRequest
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import getCurrentUserId

class GetUserCollections(
    private val collectionService: CollectionService,
    private val collectionResourceFactory: CollectionResourceFactory
) {
    operator fun invoke(projection: CollectionsController.Projections): List<CollectionResource> {
        return collectionService
            .getByOwner(getCurrentUserId(), PageRequest(0, 10)).elements
            .map { collectionResourceFactory.buildCollectionResource(it, projection) }
    }
}