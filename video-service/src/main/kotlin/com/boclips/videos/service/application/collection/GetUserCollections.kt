package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import getCurrentUserId

class GetUserCollections(
    private val collectionService: CollectionService,
    private val collectionResourceFactory: CollectionResourceFactory
) {
    operator fun invoke(projection: CollectionsController.Projections) = collectionService
        .getByOwner(getCurrentUserId())
        .map(
            when (projection) {
                CollectionsController.Projections.list -> collectionResourceFactory::buildCollectionListResource
                CollectionsController.Projections.details -> collectionResourceFactory::buildCollectionDetailsResource
            }
        )
}