package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionCreationException
import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionReadService
import com.boclips.videos.service.domain.service.collection.CreateCollectionCommand
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest

class CreateCollection(
    private val collectionRepository: CollectionRepository,
    private val addVideoToCollection: AddVideoToCollection,
    private val collectionSearchService: CollectionSearchService,
    private val collectionReadService: CollectionReadService
) {
    operator fun invoke(createCollectionRequest: CreateCollectionRequest, requester: User): Collection {
        val title = getOrThrow(createCollectionRequest.title, "title")
        val collection = collectionRepository.create(
            CreateCollectionCommand(
                owner = requester.id,
                title = title,
                description = createCollectionRequest.description,
                createdByBoclips = requester.isBoclipsEmployee,
                public = createCollectionRequest.public ?: false,
                subjects = createCollectionRequest.subjects.map { SubjectId(it) }.toSet()
            )
        )

        createCollectionRequest.videos.forEach { video ->
            addVideoToCollection(collection.id.value, video.substringAfterLast("/videos/"), requester)
        }

        collectionSearchService.upsert(sequenceOf(collection))

        return collectionReadService.find(collection.id, requester)
            ?: throw CollectionCreationException("Cannot find created collection")
    }
}
