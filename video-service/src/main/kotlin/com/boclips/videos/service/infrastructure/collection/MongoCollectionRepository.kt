package com.boclips.videos.service.infrastructure.collection

import com.boclips.users.client.model.contract.Contract
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotCreatedException
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionUpdateResult
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.service.collection.CollectionFilter
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.CollectionsUpdateCommand
import com.boclips.videos.service.domain.service.collection.CreateCollectionCommand
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
import org.litote.kmongo.addToSet
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.contains
import org.litote.kmongo.descendingSort
import org.litote.kmongo.div
import org.litote.kmongo.elemMatch
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.pull
import org.litote.kmongo.set
import java.time.Instant

class MongoCollectionRepository(
    private val mongoClient: MongoClient,
    private val collectionUpdates: CollectionUpdates = CollectionUpdates(),
    private val mongoCollectionFilterContractAdapter: MongoCollectionFilterContractAdapter,
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

    override fun getByContracts(contracts: List<Contract>, pageRequest: PageRequest): Page<Collection> {
        val criteria = and(
            contracts.map(mongoCollectionFilterContractAdapter::adapt)
        )
        return getPagedCollections(pageRequest, criteria)
    }

    override fun streamAll(consumer: (Sequence<Collection>) -> Unit) {
        val sequence = Sequence { dbCollection().find().iterator() }
            .mapNotNull(CollectionDocumentConverter::toCollection)

        consumer(sequence)
    }

    override fun streamUpdate(
        filter: CollectionFilter,
        updateCommandFactory: (Collection) -> CollectionUpdateCommand,
        updatedCollectionsConsumer: (List<CollectionUpdateResult>) -> Unit
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
            val updateCommands = windowedCollections.map(updateCommandFactory)
            val updatedCollections = bulkUpdate(updateCommands)
            logger.info { "Updated ${updatedCollections.size} collections" }
            updatedCollectionsConsumer(updatedCollections)
        }
    }

    override fun update(command: CollectionUpdateCommand): CollectionUpdateResult {
        updateOne(command.collectionId, collectionUpdates.toBson(command))
        val collection = find(command.collectionId) ?: throw CollectionNotFoundException(command.collectionId.value)
        return CollectionUpdateResult(collection, listOf(command))
    }

    override fun bulkUpdate(commands: List<CollectionUpdateCommand>): List<CollectionUpdateResult> {
        if (commands.isEmpty()) return emptyList()

        val commandsByCollectionId = commands.groupBy { it.collectionId }

        val updateDocs = commands.map { updateCommand ->
            UpdateOneModel<CollectionDocument>(
                CollectionDocument::id eq ObjectId(updateCommand.collectionId.value),
                combine(collectionUpdates.toBson(updateCommand), set(CollectionDocument::updatedAt, Instant.now()))
            )
        }

        val result = dbCollection().bulkWrite(updateDocs)
        logger.info("Bulk collection update: $result")

        return findAll(commands.map { it.collectionId }.toSet().toList())
            .map { CollectionUpdateResult(it, commandsByCollectionId.get(it.id).orEmpty()) }
    }

    override fun updateAll(
        updateCommand: CollectionsUpdateCommand,
        updatedCollectionsConsumer: (List<CollectionUpdateResult>) -> Unit
    ) {
        return when (updateCommand) {
            is CollectionsUpdateCommand.RemoveVideoFromAllCollections -> {
                streamUpdate(CollectionFilter.HasVideoId(updateCommand.videoId), { collection ->
                    CollectionUpdateCommand.RemoveVideoFromCollection(
                        collectionId = collection.id,
                        videoId = updateCommand.videoId
                    )
                }, updatedCollectionsConsumer)
            }
            is CollectionsUpdateCommand.RemoveSubjectFromAllCollections -> {
                streamUpdate(CollectionFilter.HasSubjectId(updateCommand.subjectId), { collection ->
                    CollectionUpdateCommand.RemoveSubjectFromCollection(
                        collectionId = collection.id,
                        subjectId = updateCommand.subjectId
                    )
                }, updatedCollectionsConsumer)
            }
        }
    }

    override fun delete(id: CollectionId) {
        dbCollection().deleteOne(CollectionDocument::id eq ObjectId(id.value))
        logger.info { "Deleted collection $id" }
    }

    override fun bookmark(id: CollectionId, user: UserId): Collection {
        updateOne(id, addToSet(CollectionDocument::bookmarks, user.value))
        return find(id) ?: throw CollectionNotFoundException(id.value)
    }

    override fun unbookmark(id: CollectionId, user: UserId): Collection {
        updateOne(id, pull(CollectionDocument::bookmarks, user.value))
        return find(id) ?: throw CollectionNotFoundException(id.value)
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
            pageInfo = PageInfo(
                hasMoreElements = hasMoreElements,
                totalElements = totalDocuments,
                pageRequest = pageRequest
            )
        )
    }

    private fun updateOne(id: CollectionId, update: Bson) {
        val updateWithTimestamp = combine(update, set(CollectionDocument::updatedAt, Instant.now()))

        dbCollection().updateOne(CollectionDocument::id eq ObjectId(id.value), updateWithTimestamp)
        logger.info { "Updated collection $id" }
    }

    private fun dbCollection(): MongoCollection<CollectionDocument> {
        return mongoClient
            .getDatabase(DATABASE_NAME)
            .getCollection<CollectionDocument>(collectionName)
    }
}
