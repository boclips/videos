package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceConverter
import getCurrentUserId

class GetCollection(
    private val collectionService: CollectionService,
    private val collectionResourceConverter: CollectionResourceConverter
) {
    operator fun invoke(collectionId: String?): CollectionResource {
        if (collectionId == null) {
            throw CollectionNotFoundException("unknown ID")
        }

        val userId = UserExtractor.getCurrentUserId()
        val collection = collectionService.getById(CollectionId(collectionId))

        when {
            collection == null ->
                throw CollectionNotFoundException(collectionId)
            collection.owner != userId ->
                throw CollectionAccessNotAuthorizedException(userId, collectionId)
            else ->
                return collection.let(collectionResourceConverter::toResource)
        }
    }
}