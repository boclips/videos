package com.boclips.videos.service.infrastructure.collection

import com.boclips.users.client.model.contract.Contract
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotCreatedException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionUpdateResult
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.service.collection.CollectionFilter
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
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
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.contains
import org.litote.kmongo.descendingSort
import org.litote.kmongo.div
import org.litote.kmongo.elemMatch
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
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
        logger.info("Bulk collection update: $result")

        return findAll(commands.map { it.collectionId }.toSet().toList())
            .map { CollectionUpdateResult(it, commandsByCollectionId[it.id].orEmpty()) }
    }

    private fun bsonMetadataUpdate(commands: List<CollectionUpdateCommand>): List<Bson> {
        return if(commands.any { shouldSetUpdatedTime(it) })
            listOf(set(CollectionDocument::updatedAt, Instant.now()))
        else
            emptyList()
    }

    private fun shouldSetUpdatedTime(command: CollectionUpdateCommand): Boolean {
        return when(command) {
            is CollectionUpdateCommand.Bookmark -> false
            is CollectionUpdateCommand.Unbookmark -> false
            else -> true
        }
    }

    override fun delete(id: CollectionId) {
        dbCollection().deleteOne(CollectionDocument::id eq ObjectId(id.value))
        logger.info { "Deleted collection $id" }
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

    private fun dbCollection(): MongoCollection<CollectionDocument> {
        return mongoClient
            .getDatabase(DATABASE_NAME)
            .getCollection<CollectionDocument>(collectionName)
    }
}
