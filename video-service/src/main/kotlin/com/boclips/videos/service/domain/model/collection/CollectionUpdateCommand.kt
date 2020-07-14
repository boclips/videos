package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId

sealed class CollectionUpdateCommand(val collectionId: CollectionId, val user: User) {
    class AddVideoToCollection(collectionId: CollectionId, val videoId: VideoId, user: User) :
        CollectionUpdateCommand(collectionId, user)

    class AddCollectionToCollection(collectionId: CollectionId, val subCollectionId: CollectionId, user: User) :
        CollectionUpdateCommand(collectionId, user)

    class RemoveCollectionFromCollection(collectionId: CollectionId, val subCollectionId: CollectionId, user: User) :
        CollectionUpdateCommand(collectionId, user)

    class RemoveVideoFromCollection(collectionId: CollectionId, val videoId: VideoId, user: User) :
        CollectionUpdateCommand(collectionId, user)

    class RenameCollection(collectionId: CollectionId, val title: String, user: User) :
        CollectionUpdateCommand(collectionId, user)

    class ChangeDiscoverability(collectionId: CollectionId, val discoverable: Boolean, user: User) :
        CollectionUpdateCommand(collectionId, user)

    class ChangePromotion(collectionId: CollectionId, val promoted: Boolean, user: User) :
        CollectionUpdateCommand(collectionId, user)

    class ReplaceSubjects(collectionId: CollectionId, val subjects: Set<Subject>, user: User) :
        CollectionUpdateCommand(collectionId, user)

    class ChangeAgeRange(collectionId: CollectionId, val minAge: Int, val maxAge: Int?, user: User) :
        CollectionUpdateCommand(collectionId, user)

    class RemoveSubjectFromCollection(collectionId: CollectionId, val subjectId: SubjectId, user: User) :
        CollectionUpdateCommand(collectionId, user)

    class ChangeDescription(collectionId: CollectionId, val description: String, user: User) :
        CollectionUpdateCommand(collectionId, user)

    class AddAttachment(
        collectionId: CollectionId,
        val description: String?,
        val linkToResource: String,
        val type: AttachmentType,
        user: User
    ) : CollectionUpdateCommand(collectionId, user)

    class Bookmark(collectionId: CollectionId, user: User) : CollectionUpdateCommand(collectionId, user)

    class Unbookmark(collectionId: CollectionId, user: User) : CollectionUpdateCommand(collectionId, user)

    class ReplaceVideos(
        collectionId: CollectionId,
        val videoIds: List<VideoId>,
        user: User
    ) : CollectionUpdateCommand(collectionId, user)
}
