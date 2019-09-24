package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionCreationException
import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.application.getCurrentUser
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CreateCollectionCommand
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest

class CreateCollection(
    private val collectionRepository: CollectionRepository,
    private val addVideoToCollection: AddVideoToCollection,
    private val collectionSearchService: CollectionSearchService
) {
    operator fun invoke(createCollectionRequest: CreateCollectionRequest?): Collection {
        val user = getCurrentUser()
        val title = getOrThrow(createCollectionRequest?.title, "title")
        val collection =
            collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(user.id),
                    title = title,
                    description = createCollectionRequest?.description,
                    createdByBoclips = user.boclipsEmployee,
                    public = createCollectionRequest?.public ?: false
                )
            )

        createCollectionRequest?.videos?.forEach { video ->
            addVideoToCollection(collection.id.value, video.substringAfterLast("/videos/"))
        }

        collectionSearchService.upsert(sequenceOf(collection))

        return collectionRepository.find(collection.id)
            ?: throw CollectionCreationException("Cannot find created collection")
    }
}