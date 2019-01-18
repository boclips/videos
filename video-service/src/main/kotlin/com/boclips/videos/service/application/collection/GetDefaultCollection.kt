package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class GetDefaultCollection(
        private val collectionService: CollectionService,
        private val videoToResourceConverter: VideoToResourceConverter
) {
    fun execute(): CollectionResource {
        val userId = UserExtractor.getCurrentUser().id
        val collections = collectionService.getByOwner(UserId(value = userId))

        return collections.firstOrNull()
                ?.let { convert(it) }
                ?: CollectionResource(owner = userId, title = "", videos = emptyList()
                )
    }

    private fun convert(collection: Collection): CollectionResource {
        return CollectionResource(
                owner = collection.owner.value,
                title = collection.title,
                videos = videoToResourceConverter.convert(collection.videos)
        )
    }

}