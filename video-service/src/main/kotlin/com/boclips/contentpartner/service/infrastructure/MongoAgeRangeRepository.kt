package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.domain.model.AgeRangeId
import com.boclips.contentpartner.service.domain.model.AgeRangeRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.mongodb.MongoClient
import com.mongodb.client.MongoIterable
import mu.KLogging
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class MongoAgeRangeRepository(val mongoClient: MongoClient) : AgeRangeRepository {
    companion object : KLogging() {
        const val collectionName = "ageRange"
    }

    override fun create(ageRange: AgeRange): AgeRange {
        getAgeRangeCollection().insertOne(
            AgeRangeDocumentConverter.toAgeRangeDocument(ageRange)
        )

        val createdAgeRange = findById(ageRange.id) ?: throw ResourceNotFoundApiException(
            error = "AgeRange not found",
            message = "one"
        )

        logger.info { "Created new age range: ${createdAgeRange.label}" }

        return createdAgeRange
    }

    override fun findById(id: AgeRangeId): AgeRange? {
        return findByQuery(toBsonIdFilter(id))
    }

    private fun findByQuery(mongoQuery: Bson): AgeRange? {
        return getAgeRangeCollection().findOne(mongoQuery)
            ?.let { document: AgeRangeDocument ->
                AgeRangeDocumentConverter.toAgeRange(
                    document
                )
            }
    }

    override fun findAll(): MongoIterable<AgeRange> =
        getAgeRangeCollection().find()
            .map { AgeRangeDocumentConverter.toAgeRange(it) }

    private fun getAgeRangeCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<AgeRangeDocument>(
            collectionName
        )

    private fun toBsonIdFilter(id: AgeRangeId): Bson {
        return AgeRangeDocument::id eq id.value
    }
}
