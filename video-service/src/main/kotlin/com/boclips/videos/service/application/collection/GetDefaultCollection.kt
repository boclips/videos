package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class GetDefaultCollection(
        val collectionService: CollectionService,
        val videoToResourceConverter: VideoToResourceConverter
) {
    fun execute(): CollectionResource {

        val collections = collectionService.getByOwner(UserExtractor.getCurrentUser().id)

        return collections.firstOrNull()
                ?.let { convert(it) }
                ?: CollectionResource(owner = UserExtractor.getCurrentUser().id, title = "", videos = emptyList()
        )
    }

    private fun convert(collection: Collection): CollectionResource {
        return CollectionResource(
                owner = collection.owner,
                title = collection.title,
                videos = videoToResourceConverter.convert(collection.videos)
        )
    }

}