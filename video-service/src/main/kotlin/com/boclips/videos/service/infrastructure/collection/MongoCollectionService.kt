package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotCreatedException
import com.boclips.videos.service.domain.service.collection.AddVideoToCollectionCommand
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.RemoveVideoFromCollectionCommand
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
        val collectionId = CollectionId(value = ObjectId().toHexString())
        val document = CollectionDocument(
            id = collectionId.value,
            owner = owner.value,
            title = title,
            videos = emptyList(),
            updatedAt = Instant.now()
        )

        dbCollection().insertOne(document)
        return getById(collectionId) ?: throw CollectionNotCreatedException("Failed to create collection $collectionId")
    }

    override fun getById(id: CollectionId): Collection? {
        val collectionDocument = dbCollection().findOne(CollectionDocument::id eq id.value)

        MongoCollectionService.logger.info { "Found collection ${id.value}: $collectionDocument" }

        return toCollection(collectionDocument)
    }

    override fun getByOwner(owner: UserId): List<Collection> {
        val collectionsDocuments = dbCollection()
            .find(CollectionDocument::owner eq owner.value)
            .mapNotNull(this::toCollection)

        MongoCollectionService.logger.info { "Found ${collectionsDocuments.size} collections for user ${owner.value}" }

        return collectionsDocuments
    }

    override fun update(id: CollectionId, updateCommand: CollectionUpdateCommand) {
        when (updateCommand) {
            is AddVideoToCollectionCommand -> addVideo(id, videoService.get(updateCommand.videoId).asset.assetId)
            is RemoveVideoFromCollectionCommand -> removeVideo(id, updateCommand.videoId)
            else -> throw Error("Not supported update: $updateCommand")
        }
    }

    private fun removeVideo(collectionId: CollectionId, assetId: AssetId) {
        updateOne(collectionId, pull(CollectionDocument::videos, assetId.value))
    }

    private fun addVideo(id: CollectionId, assetId: AssetId) {
        updateOne(id, addToSet(CollectionDocument::videos, assetId.value))
    }

    private fun updateOne(id: CollectionId, update: Bson) {
        val updatesWithTimestamp = combine(
            update,
            set(CollectionDocument::updatedAt, Instant.now())
        )

        dbCollection().updateOne(CollectionDocument::id eq id.value, updatesWithTimestamp)
    }

    private fun dbCollection(): MongoCollection<CollectionDocument> {
        return mongoClient
            .getDatabase(DATABASE_NAME)
            .getCollection<CollectionDocument>(collectionName)
    }

    private fun toCollection(collectionDocument: CollectionDocument?): Collection? {
        if (collectionDocument == null) return null
        val assetIds = collectionDocument.videos.map { AssetId(value = it) }

        return Collection(
            id = CollectionId(value = collectionDocument.id),
            title = collectionDocument.title,
            owner = UserId(value = collectionDocument.owner),
            videos = videoService.get(assetIds),
            updatedAt = collectionDocument.updatedAt
        )
    }
}