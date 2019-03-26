package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionCreationException
import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import getCurrentUser

class CreateCollection(
    private val collectionService: CollectionService,
    private val addVideoToCollection: AddVideoToCollection
) {
    operator fun invoke(createCollectionRequest: CreateCollectionRequest?): Collection {
        val user = getCurrentUser()
        val title = getOrThrow(createCollectionRequest?.title, "title")
        val collection = collectionService.create(owner = UserId(user.id), title = title, createdByBoclips = user.boclipsEmployee)

        createCollectionRequest?.videos?.forEach { video ->
            addVideoToCollection(collection.id.value, video.substringAfterLast("/videos/"))
        }

        return collectionService.getById(collection.id) ?: throw CollectionCreationException("Cannot find created collection")
    }
}