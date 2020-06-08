package com.boclips.contentpartner.service.infrastructure.contract.legalrestrictions

import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestriction
import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestrictionsRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import mu.KLogging
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class MongoContractLegalRestrictionsRepository(private val mongoClient: MongoClient) :
    ContractLegalRestrictionsRepository {
    companion object : KLogging() {
        const val COLLECTION_NAME = "contractLegalRestrictions"
    }

    private fun getCollection(): MongoCollection<ContractLegalRestrictionDocument> {
        return mongoClient.getDatabase(DATABASE_NAME).getCollection<ContractLegalRestrictionDocument>(
            COLLECTION_NAME
        )
    }

    override fun create(text: String): ContractLegalRestriction {
        val document =
            ContractLegalRestrictionDocument(
                id = ObjectId(),
                text = text
            )

        getCollection().insertOne(document)

        return find(document.id) ?: throw IllegalStateException("This should never happen")
    }

    private fun find(id: ObjectId): ContractLegalRestriction? {
        return getCollection().findOne(ContractLegalRestrictionDocument::id eq id)?.toContractLegalRestriction()
    }

    override fun findAll(): List<ContractLegalRestriction> {
        return getCollection().find().map { it.toContractLegalRestriction() }.toList()
    }
}
