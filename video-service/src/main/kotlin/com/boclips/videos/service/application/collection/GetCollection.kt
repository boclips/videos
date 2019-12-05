package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetCollection(
    private val collectionResourceFactory: CollectionResourceFactory,
    private val collectionAccessService: CollectionAccessService,
    private val collectionRepository: CollectionRepository
) {
    operator fun invoke(collectionId: String, projection: Projection? = Projection.list): CollectionResource {
        val collection = collectionRepository.find(CollectionId(value = collectionId))
            ?: throw CollectionNotFoundException(collectionId)

        if (!collectionAccessService.hasReadAccess(collection)) {
            throw CollectionAccessNotAuthorizedException(getCurrentUserId(), collectionId)
        }

        return collectionRepository.find(CollectionId(value = collectionId))
            ?.let {
                when (projection) {
                    Projection.details -> collectionResourceFactory.buildCollectionDetailsResource(it)
                    else -> collectionResourceFactory.buildCollectionListResource(it)
                }
            } ?: throw CollectionNotFoundException(collectionId)
    }
}
