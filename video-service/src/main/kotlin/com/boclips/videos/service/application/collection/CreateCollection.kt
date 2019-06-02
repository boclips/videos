package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionCreationException
import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.UserId
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import getCurrentUser

class CreateCollection(
    private val collectionRepository: CollectionRepository,
    private val addVideoToCollection: AddVideoToCollection
) {
    operator fun invoke(createCollectionRequest: CreateCollectionRequest?): Collection {
        val user = getCurrentUser()
        val title = getOrThrow(createCollectionRequest?.title, "title")
        val collection =
            collectionRepository.create(owner = UserId(user.id), title = title, createdByBoclips = user.boclipsEmployee)

        createCollectionRequest?.videos?.forEach { video ->
            addVideoToCollection(collection.id.value, video.substringAfterLast("/videos/"))
        }

        return collectionRepository.find(collection.id)
            ?: throw CollectionCreationException("Cannot find created collection")
    }
}