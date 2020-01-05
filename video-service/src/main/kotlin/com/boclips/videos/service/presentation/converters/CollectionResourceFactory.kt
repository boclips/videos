package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.collection.CollectionResource
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.projections.Projection

class CollectionResourceFactory(
    private val videoToResourceConverter: VideoToResourceConverter,
    private val subjectToResourceConverter: SubjectToResourceConverter,
    private val attachmentToResourceConverter: AttachmentToResourceConverter,
    private val videoService: VideoService
) {
    fun buildCollectionResource(collection: Collection, projection: Projection, user: User) =
        when (projection) {
            Projection.list -> buildCollectionListResource(collection, user)
            Projection.details -> buildCollectionDetailsResource(collection, user)
        }

    fun buildCollectionDetailsResource(collection: Collection, user: User): CollectionResource {
        return CollectionResource(
            id = collection.id.value,
            owner = collection.owner.value,
            title = collection.title,
            videos = videoToResourceConverter.convertVideos(
                videoService.getPlayableVideos(collection.videos, user.accessRules.videoAccess),
                user
            ),
            updatedAt = collection.updatedAt,
            public = collection.isPublic,
            mine = collection.isOwner(user),
            bookmarked = collection.isBookmarkedBy(user),
            createdBy = collection.createdBy(),
            subjects = subjectToResourceConverter.wrapSubjectIdsInResource(collection.subjects),
            ageRange = AgeRangeToResourceConverter.convert(collection.ageRange),
            description = collection.description,
            attachments = attachmentToResourceConverter.wrapAttachmentsInResource(collection.attachments)
        )
    }

    fun buildCollectionListResource(
        collection: Collection,
        user: User
    ): CollectionResource {
        return CollectionResource(
            id = collection.id.value,
            owner = collection.owner.value,
            title = collection.title,
            videos = videoToResourceConverter.convertVideoIds(collection.videos),
            updatedAt = collection.updatedAt,
            public = collection.isPublic,
            mine = collection.isOwner(user),
            bookmarked = collection.isBookmarkedBy(user),
            createdBy = collection.createdBy(),
            subjects = subjectToResourceConverter.wrapSubjectIdsInResource(collection.subjects),
            ageRange = AgeRangeToResourceConverter.convert(collection.ageRange),
            description = collection.description,
            attachments = attachmentToResourceConverter.wrapAttachmentsInResource(collection.attachments)
        )
    }
}
