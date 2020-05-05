package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.collection.CollectionBookmarkService

class BookmarkCollection(private val bookmarkService: CollectionBookmarkService) {
    operator fun invoke(collectionId: String, user: User) {
        bookmarkService.bookmark(collectionId = CollectionId(collectionId), user = user)
    }
}
