package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId

sealed class CollectionUpdateCommand(val collectionId: CollectionId) {
    class AddVideoToCollection(collectionId: CollectionId, val videoId: VideoId) : CollectionUpdateCommand(collectionId)
    class RemoveVideoFromCollection(collectionId: CollectionId, val videoId: VideoId) :
        CollectionUpdateCommand(collectionId)

    class BulkUpdateCollectionVideos(collectionId: CollectionId, val videoIds: List<VideoId>) :
        CollectionUpdateCommand(collectionId)

    class RenameCollection(collectionId: CollectionId, val title: String) : CollectionUpdateCommand(collectionId)
    class ChangeVisibility(collectionId: CollectionId, val isPublic: Boolean) : CollectionUpdateCommand(collectionId)
    class ReplaceSubjects(collectionId: CollectionId, val subjects: Set<Subject>) :
        CollectionUpdateCommand(collectionId)

    class ChangeAgeRange(collectionId: CollectionId, val minAge: Int, val maxAge: Int?) :
        CollectionUpdateCommand(collectionId)

    class RemoveSubjectFromCollection(collectionId: CollectionId, val subjectId: SubjectId) :
        CollectionUpdateCommand(collectionId)

    class ChangeDescription(collectionId: CollectionId, val description: String) : CollectionUpdateCommand(collectionId)
    class AddAttachment(
        collectionId: CollectionId,
        val description: String?,
        val linkToResource: String,
        val type: AttachmentType
    ) : CollectionUpdateCommand(collectionId)

    class Bookmark(
        collectionId: CollectionId,
        val userId: UserId
    ) : CollectionUpdateCommand(collectionId)

    class Unbookmark(
        collectionId: CollectionId,
        val userId: UserId
    ) : CollectionUpdateCommand(collectionId)
}
