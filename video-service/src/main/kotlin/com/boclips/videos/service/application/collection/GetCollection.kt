package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetCollection(
    private val collectionResourceFactory: CollectionResourceFactory,
    private val collectionService: CollectionService
) {
    operator fun invoke(
        collectionId: String, projection: Projection? = Projection.list, user: User
    ): CollectionResource {
        val collection = collectionService.find(CollectionId(value = collectionId), user)
            ?: throw CollectionNotFoundException(collectionId)

        return when (projection) {
            Projection.details -> collectionResourceFactory.buildCollectionDetailsResource(collection, user)
            else -> collectionResourceFactory.buildCollectionListResource(collection, user)
        }
    }
}
