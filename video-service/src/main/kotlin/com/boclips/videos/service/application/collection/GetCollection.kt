package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetCollection(
    private val collectionResourceFactory: CollectionResourceFactory,
    private val collectionService: CollectionService
) {
    operator fun invoke(collectionId: String, projection: Projection? = Projection.list): CollectionResource {
        val resourceWrapper = when (projection) {
            Projection.details -> collectionResourceFactory::buildCollectionDetailsResource
            else -> collectionResourceFactory::buildCollectionListResource
        }

        return collectionService.getReadableCollectionOrThrow(collectionId)
            .let(resourceWrapper)
    }
}