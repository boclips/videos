package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.infrastructure.DocumentWithId
import com.boclips.videos.service.infrastructure.attachment.AttachmentDocument
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.addToSet
import org.litote.kmongo.combine
import org.litote.kmongo.pull
import org.litote.kmongo.set

class CollectionUpdates {
    companion object : KLogging()

    fun toBson(command: CollectionUpdateCommand): Bson {
        return when (command) {
            is CollectionUpdateCommand.AddVideoToCollection -> addVideo(
                command.collectionId,
                command.videoId
            )
            is CollectionUpdateCommand.RemoveVideoFromCollection -> removeVideo(
                command.collectionId,
                command.videoId
            )
            is CollectionUpdateCommand.RenameCollection -> renameCollection(
                command.collectionId,
                command.title
            )
            is CollectionUpdateCommand.ChangeVisibility -> changeVisibility(
                command.collectionId,
                command.isPublic
            )
            is CollectionUpdateCommand.ReplaceSubjects -> replaceSubjects(
                command.collectionId,
                command.subjects
            )
            is CollectionUpdateCommand.ChangeAgeRange -> replaceAgeRange(
                command.collectionId,
                command.minAge,
                command.maxAge
            )
            is CollectionUpdateCommand.RemoveSubjectFromCollection -> removeSubject(
                command.collectionId,
                command.subjectId
            )
            is CollectionUpdateCommand.ChangeDescription -> changeDescription(
                command.collectionId,
                command.description
            )
            is CollectionUpdateCommand.AddAttachment -> replaceAttachment(
                command.collectionId,
                command.description,
                command.linkToResource,
                command.type
            )
            is CollectionUpdateCommand.BulkUpdateCollectionVideos -> bulkUpdateVideos(
                command.collectionId,
                command.videoIds
            )
            is CollectionUpdateCommand.Bookmark -> addToSet(CollectionDocument::bookmarks, command.userId.value)
            is CollectionUpdateCommand.Unbookmark -> pull(CollectionDocument::bookmarks, command.userId.value)
        }
    }

    private fun replaceAttachment(collectionId: CollectionId, description: String?, linkToResource: String, type: AttachmentType): Bson {
        logger.info { "Prepare replacing attachment for collection $collectionId" }
        return set(
            CollectionDocument::attachments, listOf(
                AttachmentDocument(
                    id = ObjectId(),
                    description = description,
                    linkToResource = linkToResource,
                    type = when (type) {
                        AttachmentType.LESSON_PLAN -> "LESSON_PLAN"
                    }
                )
            )
        )
    }

    private fun replaceAgeRange(collectionId: CollectionId, min: Int, max: Int?): Bson {
        logger.info { "Prepare replacing age range for collection $collectionId" }
        return combine(
            set(CollectionDocument::ageRangeMin, min),
            set(CollectionDocument::ageRangeMax, max)
        )
    }

    private fun replaceSubjects(collectionId: CollectionId, subjects: Set<Subject>): Bson {
        logger.info { "Prepare replacing subjects for collection $collectionId" }
        return set(
            CollectionDocument::subjects,
            subjects.map { SubjectDocument(id = ObjectId(it.id.value), name = it.name) }
        )
    }

    private fun removeVideo(collectionId: CollectionId, videoId: VideoId): Bson {
        logger.info { "Prepare video for removal from collection $collectionId" }
        return pull(CollectionDocument::videos, videoId.value)
    }

    private fun removeSubject(collectionId: CollectionId, subjectId: SubjectId): Bson {
        logger.info { "Prepare subject for removal from collection $collectionId" }
        return pull(CollectionDocument::subjects, DocumentWithId(_id = ObjectId(subjectId.value)))
    }

    private fun addVideo(collectionId: CollectionId, videoId: VideoId): Bson {
        logger.info { "Prepare video for addition to collection $collectionId" }
        return addToSet(CollectionDocument::videos, videoId.value)
    }

    private fun bulkUpdateVideos(collectionId: CollectionId, videoIds: List<VideoId>): Bson {
        logger.info { "Collection $collectionId will have it's videos bulk updated" }
        return set(CollectionDocument::videos, videoIds.map { it.value })
    }

    private fun renameCollection(collectionId: CollectionId, title: String): Bson {
        logger.info { "Prepare renaming of video in collection $collectionId" }
        return set(CollectionDocument::title, title)
    }

    private fun changeVisibility(collectionId: CollectionId, isPublic: Boolean): Bson {
        val visibility = if (isPublic) CollectionVisibilityDocument.PUBLIC else CollectionVisibilityDocument.PRIVATE

        logger.info { "Prepare visibility change of collection $collectionId to $visibility" }
        return set(CollectionDocument::visibility, visibility)
    }

    private fun changeDescription(collectionId: CollectionId, description: String): Bson {
        logger.info { "Prepare for replacing description of collection $collectionId" }

        return set(CollectionDocument::description, description)
    }
}
