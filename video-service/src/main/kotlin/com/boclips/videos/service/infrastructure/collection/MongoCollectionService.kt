package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotCreatedException
import com.boclips.videos.service.domain.service.collection.AddVideoToCollectionCommand
import com.boclips.videos.service.domain.service.collection.ChangeVisibilityCommand
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.RemoveVideoFromCollectionCommand
import com.boclips.videos.service.domain.service.collection.RenameCollectionCommand
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.addToSet
import org.litote.kmongo.combine
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.pull
import org.litote.kmongo.set
import java.time.Instant

class MongoCollectionService(
    private val mongoClient: MongoClient,
    private val videoService: VideoService
) : CollectionService {
    companion object : KLogging() {
        const val collectionName = "collections"
    }

    override fun create(owner: UserId, title: String): Collection {
        val objectId = ObjectId()
        val collectionId = CollectionId(value = objectId.toHexString())
        val document = CollectionDocument(
            id = objectId,
            owner = owner.value,
            title = title,
            videos = emptyList(),
            updatedAt = Instant.now(),
            visibility = CollectionVisibilityDocument.PRIVATE
        )

        dbCollection().insertOne(document)
        return getById(collectionId) ?: throw CollectionNotCreatedException("Failed to create collection $collectionId")
    }

    override fun getById(id: CollectionId): Collection? {
        val collectionDocument = dbCollection().findOne(CollectionDocument::id eq ObjectId(id.value))

        logger.info { "Found collection ${id.value}: $collectionDocument" }

        return toCollection(collectionDocument)
    }

    override fun getByOwner(owner: UserId): List<Collection> {
        val collectionsDocuments = dbCollection()
            .find(CollectionDocument::owner eq owner.value)
            .mapNotNull(this::toCollection)

        logger.info { "Found ${collectionsDocuments.size} collections for user ${owner.value}" }

        return collectionsDocuments
    }

    override fun update(id: CollectionId, updateCommand: CollectionUpdateCommand) {
        when (updateCommand) {
            is AddVideoToCollectionCommand -> addVideo(id, videoService.get(updateCommand.videoId).asset.assetId)
            is RemoveVideoFromCollectionCommand -> removeVideo(id, updateCommand.videoId)
            is RenameCollectionCommand -> renameCollection(id, updateCommand.title)
            is ChangeVisibilityCommand -> changeVisibility(id, updateCommand.isPublic)
            else -> throw Error("Not supported update: $updateCommand")
        }
    }

    override fun delete(collectionId: CollectionId) {
        dbCollection().deleteOne(CollectionDocument::id eq ObjectId(collectionId.value))
        logger.info { "Deleted collection $collectionId" }
    }

    private fun removeVideo(collectionId: CollectionId, assetId: AssetId) {
        updateOne(collectionId, pull(CollectionDocument::videos, assetId.value))
        logger.info { "Removed video from collection $collectionId" }
    }

    private fun addVideo(collectionId: CollectionId, assetId: AssetId) {
        updateOne(collectionId, addToSet(CollectionDocument::videos, assetId.value))
        logger.info { "Added video from collection $collectionId" }
    }

    private fun renameCollection(collectionId: CollectionId, title: String) {
        updateOne(collectionId, set(CollectionDocument::title, title))
        logger.info { "Renamed collection $collectionId" }
    }

    private fun changeVisibility(collectionId: CollectionId, isPublic: Boolean) {
        val visibility = if (isPublic) CollectionVisibilityDocument.PUBLIC else CollectionVisibilityDocument.PRIVATE

        updateOne(collectionId, set(CollectionDocument::visibility, visibility))
        logger.info { "Changed visibility of collection $collectionId to $visibility" }
    }

    private fun updateOne(id: CollectionId, update: Bson) {
        val updatesWithTimestamp = combine(
            update,
            set(CollectionDocument::updatedAt, Instant.now())
        )

        dbCollection().updateOne(CollectionDocument::id eq ObjectId(id.value), updatesWithTimestamp)
    }

    private fun dbCollection(): MongoCollection<CollectionDocument> {
        return mongoClient
            .getDatabase(DATABASE_NAME)
            .getCollection<CollectionDocument>(collectionName)
    }

    private fun toCollection(collectionDocument: CollectionDocument?): Collection? {
        if (collectionDocument == null) return null
        val assetIds = collectionDocument.videos.map { AssetId(value = it) }
        val isPubliclyVisible = collectionDocument.visibility == CollectionVisibilityDocument.PUBLIC

        return Collection(
            id = CollectionId(value = collectionDocument.id.toHexString()),
            title = collectionDocument.title,
            owner = UserId(value = collectionDocument.owner),
            videos = assetIds,
            updatedAt = collectionDocument.updatedAt,
            isPublic = isPubliclyVisible
        )
    }
}