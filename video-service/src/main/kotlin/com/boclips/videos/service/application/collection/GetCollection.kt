package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetCollection(
    private val collectionResourceFactory: CollectionResourceFactory,
    private val collectionAccessService: CollectionAccessService
) {
    operator fun invoke(collectionId: String, projection: Projection? = Projection.list): CollectionResource {
        val resourceWrapper = when (projection) {
            Projection.details -> collectionResourceFactory::buildCollectionDetailsResource
            else -> collectionResourceFactory::buildCollectionListResource
        }

        return collectionAccessService.getReadableCollectionOrThrow(collectionId)
            .let(resourceWrapper)
    }
}
