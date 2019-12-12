package com.boclips.videos.service.presentation.collections

import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.ageRange.AgeRangeToResourceConverter
import com.boclips.videos.service.presentation.attachments.AttachmentToResourceConverter
import com.boclips.videos.service.presentation.subject.SubjectToResourceConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

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
            videos = videoToResourceConverter.wrapVideosInResource(
                videoService.getPlayableVideo(collection.videos),
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
            videos = videoToResourceConverter.wrapVideoIdsInResource(collection.videos),
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
