package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.api.request.collection.UpdateCollectionRequest
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.config.security.UserRoles.BACKOFFICE
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService
import com.boclips.videos.service.domain.service.collection.CollectionIndex

class UpdateCollection(
    private val collectionIndex: CollectionIndex,
    private val collectionRepository: CollectionRepository,
    private val collectionUpdatesConverter: CollectionUpdatesConverter,
    private val collectionRetrievalService: CollectionRetrievalService
) {
    operator fun invoke(collectionId: String, updateCollectionRequest: UpdateCollectionRequest?, requester: User) {
        updateCollectionRequest?.promoted?.let {
            if (!UserExtractor.currentUserHasRole(BACKOFFICE)) {
                throw OperationForbiddenException()
            }
        }

        val collection = collectionRetrievalService.findWritable(CollectionId(value = collectionId), user = requester)
            ?: throw CollectionNotFoundException(collectionId)

        val commands = collectionUpdatesConverter.convert(collection.id, updateCollectionRequest, requester)

        collectionRepository.update(*commands)

        collectionRepository.find(collection.id)?.let { updatedCollection ->
            collectionIndex.upsert(sequenceOf(updatedCollection))
        }
    }
}
