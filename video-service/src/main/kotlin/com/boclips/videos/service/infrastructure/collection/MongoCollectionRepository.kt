package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotCreatedException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.CollectionsUpdateCommand
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import mu.KLogging
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.bson.types.ObjectId.isValid
import org.litote.kmongo.`in`
import org.litote.kmongo.addToSet
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.contains
import org.litote.kmongo.descendingSort
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.pull
import org.litote.kmongo.set
import java.time.Instant

class MongoCollectionRepository(
    private val mongoClient: MongoClient,
    private val collectionUpdates: CollectionUpdates = CollectionUpdates()
) : CollectionRepository {
    companion object : KLogging() {
        const val collectionName = "collections"
    }

    private val publicCollectionCriteria = CollectionDocument::visibility eq CollectionVisibilityDocument.PUBLIC

    override fun create(
        owner: UserId,
        title: String,
        createdByBoclips: Boolean,
        public: Boolean
    ): Collection {
        val objectId = ObjectId()
        val collectionId = CollectionId(value = objectId.toHexString())
        val document = CollectionDocument(
            id = objectId,
            owner = owner.value,
            title = title,
            videos = emptyList(),
            updatedAt = Instant.now(),
            visibility = if (public) CollectionVisibilityDocument.PUBLIC else CollectionVisibilityDocument.PRIVATE,
            createdByBoclips = createdByBoclips,
            subjects = emptySet()
        )

        dbCollection().insertOne(document)
        return find(collectionId) ?: throw CollectionNotCreatedException("Failed to create collection $collectionId")
    }

    override fun createWithViewers(owner: UserId, title: String, viewerIds: List<String>): Collection {
        val objectId = ObjectId()
        val collectionId = CollectionId(value = objectId.toHexString())
        val document = CollectionDocument(
            id = objectId,
            owner = owner.value,
            title = title,
            videos = emptyList(),
            updatedAt = Instant.now(),
            visibility = CollectionVisibilityDocument.PRIVATE,
            createdByBoclips = false,
            subjects = emptySet(),
            viewerIds = viewerIds
        )

        dbCollection().insertOne(document)
        return find(collectionId) ?: throw CollectionNotCreatedException("Failed to create collection $collectionId")
    }

    override fun find(id: CollectionId): Collection? {
        if (!isValid(id.value)) {
            return null
        }

        val collectionDocument = dbCollection().findOne(CollectionDocument::id eq ObjectId(id.value))
        logger.info { "Found collection ${id.value}: $collectionDocument" }

        return CollectionDocumentConverter.toCollection(collectionDocument)
    }

    override fun findAll(ids: List<CollectionId>): List<Collection> {
        val objectIds = ids.filter { isValid(it.value) }.map { ObjectId(it.value) }

        val collections: Map<CollectionId, Collection> = dbCollection().find(CollectionDocument::id `in` objectIds)
            .mapNotNull(CollectionDocumentConverter::toCollection)
            .map { it.id to it }.toMap()

        return ids.mapNotNull { id -> collections[id] }
    }

    override fun findAllBySubject(subjectId: SubjectId): List<Collection> {
        return dbCollection().find(CollectionDocument::subjects / SubjectDocument::id eq ObjectId(subjectId.value))
            .mapNotNull(CollectionDocumentConverter::toCollection)
    }

    override fun getByOwner(owner: UserId, pageRequest: PageRequest): Page<Collection> {
        val criteria = CollectionDocument::owner eq owner.value
        return getPagedCollections(pageRequest, criteria)
    }

    override fun getByViewer(viewer: UserId, pageRequest: PageRequest): Page<Collection> {
        val criteria = CollectionDocument::viewerIds contains viewer.value
        return getPagedCollections(pageRequest, criteria)
    }

    override fun getBookmarkedByUser(pageRequest: PageRequest, bookmarkedBy: UserId): Page<Collection> {
        val criteria = and(
            publicCollectionCriteria,
            CollectionDocument::bookmarks contains bookmarkedBy.value
        )
        return getPagedCollections(pageRequest, criteria)
    }

    override fun streamAllPublic(consumer: (Sequence<Collection>) -> Unit) {
        val sequence = Sequence { dbCollection().find(publicCollectionCriteria).iterator() }
            .mapNotNull(CollectionDocumentConverter::toCollection)

        consumer(sequence)
    }

    override fun update(collectionId: CollectionId, vararg updateCommands: CollectionUpdateCommand) {
        val updateBson = updateCommands
            .fold(BsonDocument()) { partialDocument: Bson, updateCommand: CollectionUpdateCommand ->
                combine(partialDocument, collectionUpdates.toBson(collectionId, updateCommand))
            }

        updateOne(collectionId, updateBson)
    }

    override fun updateAll(updateCommand: CollectionsUpdateCommand) {
        return when (updateCommand) {
            is CollectionsUpdateCommand.RemoveVideoFromAllCollections -> {
                val allCollectionsContainingVideo = dbCollection()
                    .find(CollectionDocument::videos contains updateCommand.videoId.value)

                allCollectionsContainingVideo.forEach { collectionDocument ->
                    val command = CollectionUpdateCommand.RemoveVideoFromCollection(
                        videoId = updateCommand.videoId

                    )
                    update(CollectionId(value = collectionDocument.id.toHexString()), command)
                }
            }
            is CollectionsUpdateCommand.RemoveSubjectFromAllCollections -> {
                val allCollectionsContainingSubject = findAllBySubject(updateCommand.subjectId)

                allCollectionsContainingSubject.forEach { collectionDocument ->
                    val command = CollectionUpdateCommand.RemoveSubjectFromCollection(
                        subjectId = updateCommand.subjectId
                    )
                    update(collectionDocument.id, command)
                }
            }
        }
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
            .mapNotNull(CollectionDocumentConverter::toCollection)

        val totalDocuments = dbCollection().countDocuments(criteria)
        val hasMoreElements = totalDocuments > (pageRequest.size + 1) * pageRequest.page
        logger.info { "Found ${collections.size} public collections" }

        return Page(
            elements = collections,
            pageInfo = PageInfo(hasMoreElements = hasMoreElements)
        )
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
}
