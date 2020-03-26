package com.boclips.videos.service.application.collection

import com.boclips.videos.api.request.collection.CreateCollectionRequest
import com.boclips.videos.service.application.collection.exceptions.CollectionCreationException
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionCreationService
import com.boclips.videos.service.domain.service.collection.CollectionSearchService

class CreateCollection(
    private val collectionCreationService: CollectionCreationService,
    private val collectionSearchService: CollectionSearchService
) {
    operator fun invoke(createCollectionRequest: CreateCollectionRequest, requester: User): Collection {
        val createCollectionCommand = CreateCollectionCommand(
            owner = requester.id,
            title = createCollectionRequest.title!!,
            description = createCollectionRequest.description,
            createdByBoclips = requester.isBoclipsEmployee,
            public = createCollectionRequest.public ?: false,
            subjects = createCollectionRequest.subjects.map { SubjectId(it) }.toSet()
        )

        val videoIds = createCollectionRequest.videos.map { video ->
            VideoId(value = video.substringAfterLast("/videos/"))
        }

        return collectionCreationService.create(createCollectionCommand, videoIds, requester)
            ?.also { createdCollection ->
                collectionSearchService.upsert(sequenceOf(createdCollection))
            } ?: throw CollectionCreationException("Cannot find created collection")
    }
}
