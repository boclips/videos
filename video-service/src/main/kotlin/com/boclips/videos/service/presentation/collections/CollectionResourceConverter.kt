package com.boclips.videos.service.presentation.collections

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class CollectionResourceConverter(
    private val videoToResourceConverter: VideoToResourceConverter,
    private val videoService: VideoService
) {
    fun toResource(collection: Collection): CollectionResource {
        return CollectionResource(
            id = collection.id.value,
            owner = collection.owner.value,
            title = collection.title,
            videos = videoToResourceConverter.convert(videoService.get(collection.videos)),
            updatedAt = collection.updatedAt
        )
    }
}