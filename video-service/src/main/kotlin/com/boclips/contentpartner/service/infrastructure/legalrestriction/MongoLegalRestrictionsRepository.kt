package com.boclips.contentpartner.service.infrastructure.legalrestriction

import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import mu.KLogging
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class MongoLegalRestrictionsRepository(private val mongoClient: MongoClient) :
    LegalRestrictionsRepository {
    companion object : KLogging() {

        const val COLLECTION_NAME = "legalRestrictions"
    }

    override fun create(text: String): LegalRestriction {
        val document =
            LegalRestrictionsDocument(
                id = ObjectId.get(),
                text = text
            )

        getCollection().insertOne(document)

        return find(document.id) ?: throw IllegalStateException("This should never happen")
    }

    override fun findById(id: LegalRestrictionsId): LegalRestriction? {
        return find(ObjectId(id.value))
    }

    private fun find(id: ObjectId): LegalRestriction? {
        return getCollection().findOne(LegalRestrictionsDocument::id eq id)?.toRestrictions()
    }

    override fun findAll(): List<LegalRestriction> {
        return getCollection().find().map { it.toRestrictions() }.toList()
    }

    private fun getCollection(): MongoCollection<LegalRestrictionsDocument> {
        return mongoClient.getDatabase(DATABASE_NAME).getCollection<LegalRestrictionsDocument>(
            COLLECTION_NAME
        )
    }
}
