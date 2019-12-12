package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.InvalidAttachmentTypeException
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest

class CollectionUpdatesConverter(val subjectRepository: SubjectRepository) {
    fun convert(
        collectionId: CollectionId,
        updateCollectionRequest: UpdateCollectionRequest?,
        user: User
    ): Array<CollectionUpdateCommand> {
        updateCollectionRequest ?: return emptyArray()

        return listOfNotNull(
            updateCollectionRequest.title?.let {
                CollectionUpdateCommand.RenameCollection(
                    collectionId = collectionId,
                    title = it,
                    user = user
                )
            },
            updateCollectionRequest.isPublic?.let {
                CollectionUpdateCommand.ChangeVisibility(
                    collectionId = collectionId,
                    isPublic = it,
                    user = user
                )
            },
            updateCollectionRequest.subjects?.let {
                CollectionUpdateCommand.ReplaceSubjects(
                    collectionId = collectionId,
                    subjects = it.map { subjectId ->
                        subjectRepository.findById(SubjectId(value = subjectId))!!
                    }.toSet(),
                    user = user
                )
            },
            updateCollectionRequest.ageRange?.let { ageRange ->
                ageRange.min?.let { min ->
                    CollectionUpdateCommand.ChangeAgeRange(
                        collectionId = collectionId,
                        minAge = min,
                        maxAge = ageRange.max,
                        user = user
                    )
                }
            },
            updateCollectionRequest.description?.let {
                CollectionUpdateCommand.ChangeDescription(
                    collectionId = collectionId,
                    description = it,
                    user = user
                )
            },
            updateCollectionRequest.videos?.let { videos ->
                CollectionUpdateCommand.BulkUpdateCollectionVideos(
                    collectionId = collectionId,
                    videoIds = videos.map { VideoId(it) },
                    user = user
                )
            },

            updateCollectionRequest.attachment?.let { attachment ->
                CollectionUpdateCommand.AddAttachment(
                    collectionId = collectionId,
                    type = when (attachment.type) {
                        "LESSON_PLAN" -> AttachmentType.LESSON_PLAN
                        else -> throw InvalidAttachmentTypeException(attachment.type)
                    },
                    description = attachment.description?.let { it },
                    linkToResource = attachment.linkToResource,
                    user = user
                )
            }
        ).toTypedArray()
    }
}
