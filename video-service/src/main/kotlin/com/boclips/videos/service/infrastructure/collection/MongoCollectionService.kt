package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.Page
import com.boclips.videos.service.domain.model.PageInfo
import com.boclips.videos.service.domain.model.PageRequest
import com.boclips.videos.service.domain.model.Subject
import com.boclips.videos.service.domain.model.SubjectId
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotCreatedException
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import mu.KLogging
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.addToSet
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.contains
import org.litote.kmongo.descendingSort
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.pull
import org.litote.kmongo.set
import java.time.Instant

class MongoCollectionService(
    private val mongoClient: MongoClient,
    private val collectionUpdates: CollectionUpdates = CollectionUpdates()
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

    override fun getByOwner(owner: UserId, pageRequest: PageRequest): Page<Collection> {
        val criteria = CollectionDocument::owner eq owner.value
        return getPagedCollections(pageRequest, criteria)
    }

    override fun getPublic(pageRequest: PageRequest): Page<Collection> {
        val criteria = CollectionDocument::visibility eq CollectionVisibilityDocument.PUBLIC
        return getPagedCollections(pageRequest, criteria)
    }

    override fun getBookmarked(pageRequest: PageRequest, bookmarkedBy: UserId): Page<Collection> {
        val criteria = and(
            CollectionDocument::visibility eq CollectionVisibilityDocument.PUBLIC,
            CollectionDocument::bookmarks contains bookmarkedBy.value
        )
        return getPagedCollections(pageRequest, criteria)
    }

    override fun update(id: CollectionId, updateCommand: CollectionUpdateCommand) {
        update(id, listOf(updateCommand))
    }

    override fun update(id: CollectionId, updateCommands: List<CollectionUpdateCommand>) {
        val updateBson = updateCommands
            .fold(BsonDocument()) { partialDocument: Bson, updateCommand: CollectionUpdateCommand ->
                combine(
                    partialDocument,
                    collectionUpdates.toBson(id, updateCommand)
                )
            }

        updateOne(id, updateBson)
    }

    override fun delete(id: CollectionId) {
        dbCollection().deleteOne(CollectionDocument::id eq ObjectId(id.value))
        logger.info { "Deleted collection $id" }
    }

    override fun bookmark(id: CollectionId, user: UserId) {
        updateOne(id, addToSet(CollectionDocument::bookmarks, user.value))
    }

    override fun unbookmark(id: CollectionId, user: UserId) {
        updateOne(id, pull(CollectionDocument::bookmarks, user.value))
    }

    private fun getPagedCollections(
        pageRequest: PageRequest,
        criteria: Bson
    ): Page<Collection> {
        val offset = pageRequest.size * pageRequest.page
        val collections = dbCollection()
            .find(criteria)
            .descendingSort(CollectionDocument::updatedAt)
            .limit(pageRequest.size)
            .skip(offset)
            .mapNotNull(this::toCollection)

        val totalDocuments = dbCollection().countDocuments(criteria)
        val hasMoreElements = totalDocuments > (pageRequest.size + 1) * pageRequest.page
        logger.info { "Found ${collections.size} public collections" }

        return Page(elements = collections, pageInfo = PageInfo(hasMoreElements = hasMoreElements))
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
            createdByBoclips = collectionDocument.createdByBoclips ?: false,
            bookmarks = collectionDocument.bookmarks.map { UserId(it) }.toSet(),
            subjects = collectionDocument.subjects.orEmpty().map { subjectDocument ->
                Subject(
                    id = SubjectId(value = subjectDocument.id.toHexString()),
                    name = subjectDocument.name
                )
            }.toSet()
        )
    }
}
