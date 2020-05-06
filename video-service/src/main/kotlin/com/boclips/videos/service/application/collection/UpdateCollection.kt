package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.api.request.collection.UpdateCollectionRequest
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.config.security.UserRoles.BACKOFFICE
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.collection.CollectionUpdateService

class UpdateCollection(
    private val collectionUpdatesConverter: CollectionUpdatesConverter,
    private val collectionUpdateService: CollectionUpdateService
) {
    operator fun invoke(collectionId: String, updateCollectionRequest: UpdateCollectionRequest?, requester: User) {
        updateCollectionRequest?.promoted?.let {
            if (!UserExtractor.currentUserHasRole(BACKOFFICE)) {
                throw OperationForbiddenException()
            }
        }

        val commands = collectionUpdatesConverter.convert(
            collectionId = CollectionId(collectionId),
            updateCollectionRequest = updateCollectionRequest,
            user = requester
        )

        collectionUpdateService.updateCollectionAsOwner(updates = commands)
    }
}
