package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getReadableCollectionOrThrow
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionResourceFactory: CollectionResourceFactory
) {
    operator fun invoke(collectionId: String, projection: Projection? = Projection.list): CollectionResource {
        val resourceWrapper = when (projection) {
            Projection.details -> collectionResourceFactory::buildCollectionDetailsResource
            else -> collectionResourceFactory::buildCollectionListResource
        }

        return collectionRepository.getReadableCollectionOrThrow(collectionId)
            .let(resourceWrapper)
    }
}