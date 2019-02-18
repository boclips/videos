package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class GetDefaultCollection(
    private val collectionService: CollectionService,
    private val videoToResourceConverter: VideoToResourceConverter
) {
    operator fun invoke(): CollectionResource {
        val userId = UserExtractor.getCurrentUser().id
        val owner = UserId(value = userId)
        val collection =
            collectionService.getByOwner(owner).firstOrNull() ?:
            collectionService.create(owner = owner, title = Collection.DEFAULT_TITLE)

        return collection.let(this::convert)
    }

    private fun convert(collection: Collection): CollectionResource {
        return CollectionResource(
            id = collection.id.value,
            owner = collection.owner.value,
            title = collection.title,
            videos = videoToResourceConverter.convert(collection.videos)
        )
    }
}