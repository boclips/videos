package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.subjects.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import mu.KLogging
import org.bson.conversions.Bson
import org.litote.kmongo.addToSet
import org.litote.kmongo.combine
import org.litote.kmongo.pull
import org.litote.kmongo.set

class CollectionUpdates {
    companion object : KLogging()

    fun toBson(
        id: CollectionId,
        anyUpdateCommand: CollectionUpdateCommand
    ): Bson {
        return when (anyUpdateCommand) {
            is CollectionUpdateCommand.AddVideoToCollection -> addVideo(id, anyUpdateCommand.videoId)
            is CollectionUpdateCommand.RemoveVideoFromCollection -> removeVideo(id, anyUpdateCommand.videoId)
            is CollectionUpdateCommand.RenameCollection -> renameCollection(id, anyUpdateCommand.title)
            is CollectionUpdateCommand.ChangeVisibility -> changeVisibility(id, anyUpdateCommand.isPublic)
            is CollectionUpdateCommand.ReplaceSubjects -> replaceSubjects(id, anyUpdateCommand.subjects)
            is CollectionUpdateCommand.ChangeAgeRange -> replaceAgeRange(
                id,
                anyUpdateCommand.minAge,
                anyUpdateCommand.maxAge
            )
            is CollectionUpdateCommand.RemoveSubjectFromCollection -> removeSubject(id, anyUpdateCommand.subjectId)
        }
    }

    private fun replaceAgeRange(collectionId: CollectionId, min: Int, max: Int?): Bson {
        logger.info { "Prepare replacing age range for collection $collectionId" }
        return combine(
            set(
                CollectionDocument::ageRangeMin,
                min
            ),
            set(
                CollectionDocument::ageRangeMax,
                max
            )
        )
    }

    private fun replaceSubjects(collectionId: CollectionId, subjects: Set<SubjectId>): Bson {
        logger.info { "Prepare replacing subjects for collection $collectionId" }
        return set(
            CollectionDocument::subjects,
            subjects.map { subjectId -> subjectId.value }
        )
    }

    private fun removeVideo(collectionId: CollectionId, videoId: VideoId): Bson {
        logger.info { "Prepare video for removal from collection $collectionId" }
        return pull(CollectionDocument::videos, videoId.value)
    }

    private fun removeSubject(collectionId: CollectionId, subjectId: SubjectId): Bson {
        logger.info { "Prepare subject for removal from collection $collectionId" }
        return pull(CollectionDocument::subjects, subjectId.value)
    }

    private fun addVideo(collectionId: CollectionId, videoId: VideoId): Bson {
        logger.info { "Prepare video for addition to collection $collectionId" }
        return addToSet(CollectionDocument::videos, videoId.value)
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
}
