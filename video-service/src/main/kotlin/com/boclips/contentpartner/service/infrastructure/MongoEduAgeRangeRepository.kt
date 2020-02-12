package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.EduAgeRange
import com.boclips.contentpartner.service.domain.model.EduAgeRangeId
import com.boclips.contentpartner.service.domain.model.EduAgeRangeRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.mongodb.MongoClient
import com.mongodb.client.MongoIterable
import mu.KLogging
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class MongoEduAgeRangeRepository(val mongoClient: MongoClient) : EduAgeRangeRepository {
    companion object : KLogging() {
        const val collectionName = "ageRange"
    }

    override fun create(eduAgeRange: EduAgeRange): EduAgeRange {
        val eduAgeRangeDocument = EduAgeRangeDocumentConverter.toEduAgeRangeDocument(eduAgeRange)

        getEduAgeRangeCollection().insertOne(eduAgeRangeDocument)

        val createdEduAgeRange = findById(eduAgeRange.id) ?: throw ResourceNotFoundApiException(
            error = "AgeRange not found",
            message = "one"
        )

        logger.info { "Created new age range: ${createdEduAgeRange.label}" }

        return createdEduAgeRange
    }

    override fun findById(id: EduAgeRangeId): EduAgeRange? {
        return findByQuery(toBsonIdFilter(id))
    }

    private fun findByQuery(mongoQuery: Bson): EduAgeRange? {
        return getEduAgeRangeCollection().findOne(mongoQuery)
            ?.let { document: EduAgeRangeDocument ->
                EduAgeRangeDocumentConverter.toEduAgeRange(
                    document
                )
            }
    }

    override fun findAll(): MongoIterable<EduAgeRange> =
        getEduAgeRangeCollection().find()
            .map { EduAgeRangeDocumentConverter.toEduAgeRange(it) }

    private fun getEduAgeRangeCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<EduAgeRangeDocument>(
            collectionName
        )

    private fun toBsonIdFilter(id: EduAgeRangeId): Bson {
        return EduAgeRangeDocument::id eq id.value
    }
}