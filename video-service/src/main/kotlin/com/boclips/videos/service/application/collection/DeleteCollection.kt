package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.collection.CollectionDeletionService

class DeleteCollection(
    private val collectionDeletionService: CollectionDeletionService
) {
    operator fun invoke(collectionId: String, user: User) {
        collectionDeletionService.delete(collectionId = CollectionId(collectionId), user = user)
    }
}
