package com.boclips.videos.service.presentation.collections

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.application.collection.exceptions.InvalidCreateCollectionRequestException
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import getCurrentUserId

class CollectionResourceConverter(
    private val videoToResourceConverter: VideoToResourceConverter
) {
    fun toResource(collection: Collection): CollectionResource {
        return CollectionResource(
            id = collection.id.value,
            owner = collection.owner.value,
            title = collection.title,
            videos = videoToResourceConverter.convert(collection.videos)
        )
    }
}