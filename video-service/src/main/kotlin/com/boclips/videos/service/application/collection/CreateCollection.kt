package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionCreationException
import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionWriteService
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest

class CreateCollection(
    private val collectionWriteService: CollectionWriteService,
    private val collectionSearchService: CollectionSearchService
) {
    operator fun invoke(createCollectionRequest: CreateCollectionRequest, requester: User): Collection {
        val title = getOrThrow(createCollectionRequest.title, "title")
        val createCollectionCommand = CreateCollectionCommand(
            owner = requester.id,
            title = title,
            description = createCollectionRequest.description,
            createdByBoclips = requester.isBoclipsEmployee,
            public = createCollectionRequest.public ?: false,
            subjects = createCollectionRequest.subjects.map { SubjectId(it) }.toSet()
        )

        val videoIds = createCollectionRequest.videos.map { video ->
            VideoId(value = video.substringAfterLast("/videos/"))
        }

        return collectionWriteService.create(createCollectionCommand, videoIds, requester)
            ?.also { createdCollection ->
                collectionSearchService.upsert(sequenceOf(createdCollection))
            } ?: throw CollectionCreationException("Cannot find created collection")
    }
}
