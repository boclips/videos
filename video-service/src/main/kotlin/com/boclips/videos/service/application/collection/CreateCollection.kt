package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.application.collection.exceptions.CollectionCreationException
import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import getCurrentUserId

class CreateCollection(
    private val collectionService: CollectionService,
    private val addVideoToCollection: AddVideoToCollection
) {
    operator fun invoke(createCollectionRequest: CreateCollectionRequest) = collectionService
        .create(UserExtractor.getCurrentUserId(), getOrThrow(createCollectionRequest.title, "title"))
        .apply {
            createCollectionRequest.videos.forEach { video ->
                addVideoToCollection(id.value, video.substringAfterLast("/videos/"))
            }
        }
        .let { collectionService.getById(it.id) } ?: throw CollectionCreationException("Cannot find created collection")
}