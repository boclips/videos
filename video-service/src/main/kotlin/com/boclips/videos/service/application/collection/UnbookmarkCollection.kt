package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.collection.CollectionBookmarkService

class UnbookmarkCollection(
    private val collectionBookmarkService: CollectionBookmarkService
) {
    operator fun invoke(collectionId: String, user: User) {
        collectionBookmarkService.unbookmark(collectionId = CollectionId(collectionId), user = user)
    }
}
