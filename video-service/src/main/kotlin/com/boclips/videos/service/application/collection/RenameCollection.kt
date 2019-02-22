package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.RenameCollectionCommand

class RenameCollection(
        private val collectionService: CollectionService
) {
    operator fun invoke(collectionId: String, title: String?) = collectionService
            .update(CollectionId(collectionId), RenameCollectionCommand(getOrThrow(title, "title")))
}
