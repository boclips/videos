package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.SubjectId
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import org.bson.conversions.Bson
import org.litote.kmongo.addToSet
import org.litote.kmongo.combine
import org.litote.kmongo.pull
import org.litote.kmongo.set

class CollectionUpdates {
    fun toBson(
        id: CollectionId,
        anyUpdateCommand: CollectionUpdateCommand
    ): Bson {
        return when (anyUpdateCommand) {
            is CollectionUpdateCommand.AddVideoToCollectionCommand -> addVideo(id, anyUpdateCommand.videoId)
            is CollectionUpdateCommand.RemoveVideoFromCollectionCommand -> removeVideo(id, anyUpdateCommand.videoId)
            is CollectionUpdateCommand.RenameCollectionCommand -> renameCollection(id, anyUpdateCommand.title)
            is CollectionUpdateCommand.ChangeVisibilityCommand -> changeVisibility(id, anyUpdateCommand.isPublic)
            is CollectionUpdateCommand.ReplaceSubjectsCommand -> replaceSubjects(id, anyUpdateCommand.subjects)
            is CollectionUpdateCommand.ChangeAgeRangeCommand -> replaceAgeRange(
                id,
                anyUpdateCommand.minAge,
                anyUpdateCommand.maxAge
            )
        }
    }

    private fun replaceAgeRange(collectionId: CollectionId, min: Int, max: Int?): Bson {
        MongoCollectionService.logger.info { "Prepare replacing age range for collection $collectionId" }
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
        MongoCollectionService.logger.info { "Prepare replacing subjects for collection $collectionId" }
        return set(
            CollectionDocument::subjects,
            subjects.map { subjectId -> subjectId.value }
        )
    }

    private fun removeVideo(collectionId: CollectionId, assetId: AssetId): Bson {
        MongoCollectionService.logger.info { "Prepare video for removal from collection $collectionId" }
        return pull(CollectionDocument::videos, assetId.value)
    }

    private fun addVideo(collectionId: CollectionId, assetId: AssetId): Bson {
        MongoCollectionService.logger.info { "Prepare video for addition to collection $collectionId" }
        return addToSet(CollectionDocument::videos, assetId.value)
    }

    private fun renameCollection(collectionId: CollectionId, title: String): Bson {
        MongoCollectionService.logger.info { "Prepare renaming of video in collection $collectionId" }
        return set(CollectionDocument::title, title)
    }

    private fun changeVisibility(collectionId: CollectionId, isPublic: Boolean): Bson {
        val visibility = if (isPublic) CollectionVisibilityDocument.PUBLIC else CollectionVisibilityDocument.PRIVATE

        MongoCollectionService.logger.info { "Prepare visibility change of collection $collectionId to $visibility" }
        return set(CollectionDocument::visibility, visibility)
    }
}