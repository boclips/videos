package com.boclips.videos.service.application.collection

import com.boclips.security.utils.User
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetCollection(
    private val collectionResourceFactory: CollectionResourceFactory,
    private val collectionAccessService: CollectionAccessService,
    private val collectionRepository: CollectionRepository
) {
    operator fun invoke(
        collectionId: String,
        projection: Projection? = Projection.list,
        requester: User
    ): CollectionResource {
        val collection = collectionRepository.find(CollectionId(value = collectionId))
            ?: throw CollectionNotFoundException(collectionId)

        if (!collectionAccessService.hasReadAccess(collection, requester)) {
            throw CollectionAccessNotAuthorizedException(UserId(value = requester.id), collectionId)
        }

        return collectionRepository.find(CollectionId(value = collectionId))
            ?.let {
                when (projection) {
                    Projection.details -> collectionResourceFactory.buildCollectionDetailsResource(it, requester)
                    else -> collectionResourceFactory.buildCollectionListResource(it, requester)
                }
            } ?: throw CollectionNotFoundException(collectionId)
    }
}
