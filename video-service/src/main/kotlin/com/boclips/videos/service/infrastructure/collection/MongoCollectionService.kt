package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.Page
import com.boclips.videos.service.domain.model.PageInfo
import com.boclips.videos.service.domain.model.PageRequest
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotCreatedException
import com.boclips.videos.service.domain.service.collection.*
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import mu.KLogging
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.*
import java.time.Instant

class MongoCollectionService(
    private val mongoClient: MongoClient,
    private val videoService: VideoService
) : CollectionService {
    companion object : KLogging() {
        const val collectionName = "collections"
    }

    override fun create(owner: UserId, title: String, createdByBoclips: Boolean): Collection {
        val objectId = ObjectId()
        val collectionId = CollectionId(value = objectId.toHexString())
        val document = CollectionDocument(
            id = objectId,
            owner = owner.value,
            title = title,
            videos = emptyList(),
            updatedAt = Instant.now(),
            visibility = CollectionVisibilityDocument.PRIVATE,
            createdByBoclips = createdByBoclips
        )

        dbCollection().insertOne(document)
        return getById(collectionId) ?: throw CollectionNotCreatedException("Failed to create collection $collectionId")
    }

    override fun getById(id: CollectionId): Collection? {
        val collectionDocument = dbCollection().findOne(CollectionDocument::id eq ObjectId(id.value))

        logger.info { "Found collection ${id.value}: $collectionDocument" }

        return toCollection(collectionDocument)
    }

    override fun getByOwner(
        owner: UserId,
        pageRequest: PageRequest
    ): Page<Collection> {
        val collectionsDocuments = dbCollection()
            .find(CollectionDocument::owner eq owner.value)
            .mapNotNull(this::toCollection)

        logger.info { "Found ${collectionsDocuments.size} collections for user ${owner.value}" }

        return Page(elements=collectionsDocuments, pageInfo = PageInfo(hasMoreElements = false))
    }

    override fun getPublic(pageRequest: PageRequest): Page<Collection> {
        val publicCollectionsCriteria = CollectionDocument::visibility eq CollectionVisibilityDocument.PUBLIC
        val publicCollections =
            dbCollection().find(publicCollectionsCriteria)
                .descendingSort(CollectionDocument::updatedAt)
                .limit(pageRequest.size)
                .skip(pageRequest.size * pageRequest.page)
                .mapNotNull(this::toCollection)

        val hasMoreElements = dbCollection().countDocuments(publicCollectionsCriteria) > (pageRequest.size + 1) * pageRequest.page
        logger.info { "Found ${publicCollections.size} public collections" }

        return Page(elements = publicCollections, pageInfo = PageInfo(hasMoreElements = hasMoreElements))

    }

    override fun update(id: CollectionId, updateCommand: CollectionUpdateCommand) {
        update(id, listOf(updateCommand))
    }

    override fun update(id: CollectionId, updateCommands: List<CollectionUpdateCommand>) {
        val updateBson =
            updateCommands.fold(BsonDocument()) { partialDocument: Bson, updateCommand: CollectionUpdateCommand ->
                val commandAsBson = when (updateCommand) {
                    is AddVideoToCollectionCommand -> addVideo(
                        id,
                        videoService.get(updateCommand.videoId).asset.assetId
                    )
                    is RemoveVideoFromCollectionCommand -> removeVideo(id, updateCommand.videoId)
                    is RenameCollectionCommand -> renameCollection(id, updateCommand.title)
                    is ChangeVisibilityCommand -> changeVisibility(id, updateCommand.isPublic)
                    else -> throw Error("Not supported update: $updateCommand")
                }

                combine(
                    partialDocument,
                    commandAsBson
                )
            }

        updateOne(id, updateBson)
    }

    override fun delete(collectionId: CollectionId) {
        dbCollection().deleteOne(CollectionDocument::id eq ObjectId(collectionId.value))
        logger.info { "Deleted collection $collectionId" }
    }

    private fun removeVideo(collectionId: CollectionId, assetId: AssetId): Bson {
        logger.info { "Prepare video for removal from collection $collectionId" }
        return pull(CollectionDocument::videos, assetId.value)
    }

    private fun addVideo(collectionId: CollectionId, assetId: AssetId): Bson {
        logger.info { "Prepare video for addition to collection $collectionId" }
        return addToSet(CollectionDocument::videos, assetId.value)
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

    private fun updateOne(id: CollectionId, update: Bson) {
        val updatesWithTimestamp = combine(
            update,
            set(CollectionDocument::updatedAt, Instant.now())
        )

        dbCollection().updateOne(CollectionDocument::id eq ObjectId(id.value), updatesWithTimestamp)
        logger.info { "Updated collection $id" }
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
            isPublic = isPubliclyVisible,
            createdByBoclips = collectionDocument.createdByBoclips ?: false
        )
    }
}