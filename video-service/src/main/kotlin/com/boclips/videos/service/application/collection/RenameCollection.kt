package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.RenameCollectionCommand
import getCurrentUserId

class RenameCollection(
        private val collectionService: CollectionService
) {
    operator fun invoke(collectionId: String, title: String?) {
        getOwnedCollectionOrThrow(collectionId, collectionService)

        collectionService
                .update(CollectionId(collectionId), RenameCollectionCommand(getOrThrow(title, "title")))
    }
}
