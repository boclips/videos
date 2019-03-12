package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetCollection(
    private val collectionService: CollectionService,
    private val collectionResourceFactory: CollectionResourceFactory
) {
    operator fun invoke(collectionId: String?): CollectionResource {
        if (collectionId == null) {
            throw CollectionNotFoundException("unknown ID")
        }

        return getReadableCollectionOrThrow(collectionId, collectionService).let(collectionResourceFactory::buildCollectionDetailsResource)
    }
}