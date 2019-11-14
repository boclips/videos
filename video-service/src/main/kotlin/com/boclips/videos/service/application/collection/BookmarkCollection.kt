package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand

class BookmarkCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionService: CollectionService,
    private val collectionSearchService: CollectionSearchService
) {
    operator fun invoke(collectionId: String) {
        val collection = collectionService.getReadableCollectionOrThrow(collectionId)
        if (collection.isMine()) throw CollectionIllegalOperationException(
            getCurrentUserId(),
            collectionId,
            "bookmark your own collection"
        )

        val result = collectionRepository.update(CollectionUpdateCommand.Bookmark(collection.id, getCurrentUserId()))

        collectionSearchService.upsert(result.map { it.collection }.asSequence())
    }
}
