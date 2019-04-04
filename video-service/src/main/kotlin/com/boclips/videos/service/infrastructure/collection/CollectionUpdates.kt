package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import org.bson.conversions.Bson
import org.litote.kmongo.addToSet
import org.litote.kmongo.pull
import org.litote.kmongo.set

class CollectionUpdates {
    fun toBson(
        id: CollectionId,
        anyUpdateCommand: CollectionUpdateCommand
    ): Bson {
        return when (anyUpdateCommand) {
            is CollectionUpdateCommand.AddVideoToCollectionCommand -> addVideo(
                id,
                anyUpdateCommand.videoId
            )
            is CollectionUpdateCommand.RemoveVideoFromCollectionCommand -> removeVideo(id, anyUpdateCommand.videoId)
            is CollectionUpdateCommand.RenameCollectionCommand -> renameCollection(id, anyUpdateCommand.title)
            is CollectionUpdateCommand.ChangeVisibilityCommand -> changeVisibility(id, anyUpdateCommand.isPublic)
        }
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