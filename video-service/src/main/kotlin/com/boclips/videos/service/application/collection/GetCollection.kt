package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class GetCollection(
    private val collectionService: CollectionService,
    private val videoToResourceConverter: VideoToResourceConverter
) {
    operator fun invoke(collectionId: String?): CollectionResource {
        if (collectionId == null) {
            throw CollectionNotFoundException("unknown ID")
        }

        val collection = collectionService.getById(CollectionId(collectionId))

        if (collection == null) {
            throw CollectionNotFoundException(collectionId)
        } else {
            return collection.let(this::convert)
        }
    }

    // TODO: Dedupe with GetDefaultCollection?
    private fun convert(collection: Collection): CollectionResource {
        return CollectionResource(
            id = collection.id.value,
            owner = collection.owner.value,
            title = collection.title,
            videos = videoToResourceConverter.convert(collection.videos)
        )
    }
}