package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotCreatedException
import com.boclips.videos.service.domain.service.collection.AddVideoToCollection
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.RemoveVideoFromCollection
import com.boclips.videos.service.domain.service.video.VideoService
import com.mongodb.MongoClient
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.addToSet
import com.mongodb.client.model.Updates.pull
import mu.KLogging
import org.bson.types.ObjectId

class MongoCollectionService(
    private val mongoClient: MongoClient,
    private val documentConverter: CollectionDocumentConverter,
    private val videoService: VideoService
) : CollectionService {
    companion object : KLogging() {
        const val databaseName = "video-service-db"
        const val collectionName = "collections"
    }

    override fun create(owner: UserId): Collection {
        val collectionId = CollectionId(value = ObjectId().toHexString())

        mongoClient
            .getDatabase(databaseName)
            .getCollection(collectionName)
            .insertOne(
                documentConverter.toDocument(
                    CollectionDocument(
                        id = collectionId.value,
                        owner = owner.value,
                        title = "",
                        videos = emptyList()
                    )
                )
            )

        return getById(collectionId) ?: throw CollectionNotCreatedException("Failed to create collection $collectionId")
    }

    override fun getById(id: CollectionId): Collection? {
        val collectionDocument = mongoClient
            .getDatabase(databaseName)
            .getCollection(collectionName)
            .find(Filters.eq("_id", ObjectId(id.value)))
            .firstOrNull()
            ?.let(documentConverter::fromDocument)

        MongoCollectionService.logger.info { "Found collection ${id.value}" }

        return toCollection(collectionDocument)
    }

    override fun getByOwner(owner: UserId): List<Collection> {
        val collectionsDocuments = mongoClient
            .getDatabase(databaseName)
            .getCollection(collectionName)
            .find(Filters.eq("owner", owner.value))
            .toList()
            .map { document -> documentConverter.fromDocument(document) }

        MongoCollectionService.logger.info { "Found ${collectionsDocuments.size} collections for user ${owner.value}" }

        return collectionsDocuments.mapNotNull { toCollection(it) }
    }

    override fun update(id: CollectionId, updateCommand: CollectionUpdateCommand) {
        when (updateCommand) {
            is AddVideoToCollection -> addVideo(id, videoService.get(updateCommand.videoId))
            is RemoveVideoFromCollection -> removeVideo(id, updateCommand.videoId)
            else -> throw Error("Not supported update: $updateCommand")
        }
    }

    private fun removeVideo(collectionId: CollectionId, assetId: AssetId) {
        mongoClient
            .getDatabase(databaseName)
            .getCollection(collectionName)
            .updateOne(
                eq("_id", ObjectId(collectionId.value)),
                pull("videos", assetId.value)
            )
    }

    private fun addVideo(id: CollectionId, video: Video) {
        mongoClient
            .getDatabase(databaseName)
            .getCollection(collectionName)
            .updateOne(
                eq("_id", ObjectId(id.value)),
                addToSet("videos", video.asset.assetId.value)
            )
    }

    private fun toCollection(collectionDocument: CollectionDocument?): Collection? {
        if (collectionDocument == null) return null
        val assetIds = collectionDocument.videos.map { AssetId(value = it) }

        return Collection(
            id = CollectionId(value = collectionDocument.id),
            title = collectionDocument.title!!,
            owner = UserId(value = collectionDocument.owner),
            videos = videoService.get(assetIds)
        )
    }
}