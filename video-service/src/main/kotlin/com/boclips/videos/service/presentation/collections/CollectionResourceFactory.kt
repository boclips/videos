package com.boclips.videos.service.presentation.collections

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.Projection
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
            videos = videoToResourceConverter.wrapVideosInResource(videoService.get(collection.videos)),
            updatedAt = collection.updatedAt,
            isPublic = collection.isPublic,
            isMine = collection.isMine(),
            isBookmarked = collection.isBookmarked(),
            createdBy = collection.createdBy()
        )
    }

    fun buildCollectionListResource(collection: Collection): CollectionResource {
        return CollectionResource(
            id = collection.id.value,
            owner = collection.owner.value,
            title = collection.title,
            videos = videoToResourceConverter.wrapVideoAssetIdsInResource(collection.videos),
            updatedAt = collection.updatedAt,
            isPublic = collection.isPublic,
            isMine = collection.isMine(),
            isBookmarked = collection.isBookmarked(),
            createdBy = collection.createdBy()
        )
    }

    fun buildCollectionResource(collection: Collection, projection: Projection) =
        when (projection) {
            Projection.list -> buildCollectionListResource(collection)
            Projection.details -> buildCollectionDetailsResource(collection)
        }

}