package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionFilter
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotCreatedException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.collection.CollectionUpdateResult
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.UpdateOneModel
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.bson.types.ObjectId.isValid
import org.litote.kmongo.`in`
import org.litote.kmongo.combine
import org.litote.kmongo.contains
import org.litote.kmongo.elemMatch
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.set
import java.time.Instant

class MongoCollectionRepository(
    private val mongoClient: MongoClient,
    private val collectionUpdates: CollectionUpdates = CollectionUpdates(),
    private val batchProcessingConfig: BatchProcessingConfig,
    private val collectionSubjects: CollectionSubjects
) : CollectionRepository {
    companion object : KLogging() {
        const val collectionName = "collections"
    }

    override fun create(command: CreateCollectionCommand): Collection {
        val objectId = ObjectId()
        val collectionId = CollectionId(value = objectId.toHexString())
        val document = CollectionDocument(
            id = objectId,
            owner = command.owner.value,
            title = command.title,
            description = command.description,
            videos = emptyList(),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            visibility = if (command.public) CollectionVisibilityDocument.PUBLIC else CollectionVisibilityDocument.PRIVATE,
            createdByBoclips = command.createdByBoclips,
            subjects = collectionSubjects.getByIds(*command.subjects.toTypedArray())
        )

        dbCollection().insertOne(document)
        return find(collectionId) ?: throw CollectionNotCreatedException("Failed to create collection $collectionId")
    }

    override fun find(id: CollectionId): Collection? {
        if (!isValid(id.value)) {
            return null
        }

        val collectionDocument = dbCollection().findOne(CollectionDocument::id eq ObjectId(id.value))
        logger.info { "Found collection ${id.value}" }

        return CollectionDocumentConverter.toCollection(collectionDocument)
    }

    override fun findAll(ids: List<CollectionId>): List<Collection> {
        val objectIds = ids.filter { isValid(it.value) }.map { ObjectId(it.value) }

        val collections: Map<CollectionId, Collection> = dbCollection().find(CollectionDocument::id `in` objectIds)
            .mapNotNull(CollectionDocumentConverter::toCollection)
            .map { it.id to it }.toMap()

        return ids.mapNotNull { id -> collections[id] }
    }

    override fun streamAll(consumer: (Sequence<Collection>) -> Unit) {
        val sequence = Sequence { dbCollection().find().iterator() }
            .mapNotNull(CollectionDocumentConverter::toCollection)

        consumer(sequence)
    }

    override fun streamUpdate(
        filter: CollectionFilter,
        updateCommandFactory: (Collection) -> CollectionUpdateCommand,
        updateResultConsumer: (CollectionUpdateResult) -> Unit
    ) {
        val filterCriteria = when (filter) {
            is CollectionFilter.HasSubjectId -> CollectionDocument::subjects elemMatch (SubjectDocument::id eq ObjectId(
                filter.subjectId.value
            ))
            is CollectionFilter.HasVideoId -> CollectionDocument::videos contains filter.videoId.value
        }

        val sequence = Sequence { dbCollection().find(filterCriteria).noCursorTimeout(true).iterator() }
            .mapNotNull(CollectionDocumentConverter::toCollection)

        sequence.windowed(
            size = batchProcessingConfig.collectionBatchSize,
            step = batchProcessingConfig.collectionBatchSize,
            partialWindows = true
        ).forEachIndexed { index, windowedCollections ->
            logger.info { "Starting update batch: $index" }
            val updateCommands = windowedCollections.map(updateCommandFactory).toTypedArray()
            val updateResults = update(*updateCommands)
            logger.info { "Updated ${updateResults.size} collections" }
            updateResults.forEach(updateResultConsumer)
        }
    }

    override fun update(vararg commands: CollectionUpdateCommand): List<CollectionUpdateResult> {
        if (commands.isEmpty()) return emptyList()

        val commandsByCollectionId = commands.groupBy { it.collectionId }

        val updateDocs = commandsByCollectionId.entries.map { (collectionId, collectionCommands) ->
            UpdateOneModel<CollectionDocument>(
                CollectionDocument::id eq ObjectId(collectionId.value),
                combine(collectionCommands.map(collectionUpdates::toBson) + bsonMetadataUpdate(collectionCommands))
            )
        }

        val result = dbCollection().bulkWrite(updateDocs)
        logger.info("Updated collections: modified: ${result.modifiedCount}, deleted: ${result.deletedCount}, inserted: ${result.insertedCount}")

        return findAll(commands.map { it.collectionId }.toSet().toList())
            .map { CollectionUpdateResult(it, commandsByCollectionId[it.id].orEmpty()) }
    }

    private fun bsonMetadataUpdate(commands: List<CollectionUpdateCommand>): List<Bson> {
        return if (commands.any { shouldSetUpdatedTime(it) })
            listOf(set(CollectionDocument::updatedAt, Instant.now()))
        else
            emptyList()
    }

    private fun shouldSetUpdatedTime(command: CollectionUpdateCommand): Boolean {
        return when (command) {
            is CollectionUpdateCommand.Bookmark -> false
            is CollectionUpdateCommand.Unbookmark -> false
            else -> true
        }
    }

    override fun delete(id: CollectionId, user: User) {
        dbCollection().deleteOne(CollectionDocument::id eq ObjectId(id.value))
        logger.info { "User $user deleted collection $id" }
    }

    private fun dbCollection(): MongoCollection<CollectionDocument> {
        return mongoClient
            .getDatabase(DATABASE_NAME)
            .getCollection<CollectionDocument>(collectionName)
    }
}
