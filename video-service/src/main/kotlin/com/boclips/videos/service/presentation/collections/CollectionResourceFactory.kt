package com.boclips.videos.service.presentation.collections

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class CollectionResourceFactory(
    private val videoToResourceConverter: VideoToResourceConverter,
    private val videoService: VideoService
) {
    fun buildCollectionDetailsResource(collection: Collection): CollectionResource {
        return CollectionResource(
            id = collection.id.value,
            owner = collection.owner.value,
            title = collection.title,
            videos = videoToResourceConverter.fromVideos(videoService.get(collection.videos)),
            updatedAt = collection.updatedAt,
            isPublic = collection.isPublic
        )
    }

    fun buildCollectionListResource(collection: Collection): CollectionResource {
        return CollectionResource(
            id = collection.id.value,
            owner = collection.owner.value,
            title = collection.title,
            videos = videoToResourceConverter.fromAssetIds(collection.videos),
            updatedAt = collection.updatedAt,
            isPublic = collection.isPublic
        )
    }

    fun buildCollectionResource(collection: Collection, projection: CollectionsController.Projections) =
        when (projection) {
            CollectionsController.Projections.list -> buildCollectionListResource(collection)
            CollectionsController.Projections.details -> buildCollectionDetailsResource(collection)
        }
}