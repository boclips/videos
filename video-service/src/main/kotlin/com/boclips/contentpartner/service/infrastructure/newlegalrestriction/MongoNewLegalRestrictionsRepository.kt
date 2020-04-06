package com.boclips.contentpartner.service.infrastructure.newlegalrestriction

import com.boclips.contentpartner.service.domain.model.newlegalrestriction.NewLegalRestriction
import com.boclips.contentpartner.service.domain.model.newlegalrestriction.NewLegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.model.newlegalrestriction.SingleLegalRestriction
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import mu.KLogging
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class MongoNewLegalRestrictionsRepository(private val mongoClient: MongoClient) : NewLegalRestrictionsRepository {
    companion object : KLogging() {
        const val COLLECTION_NAME = "newLegalRestrictions"
    }

    private fun getCollection(): MongoCollection<NewLegalRestrictionDocument> {
        return mongoClient.getDatabase(DATABASE_NAME).getCollection<NewLegalRestrictionDocument>(COLLECTION_NAME)
    }

    override fun create(id: String, restrictions: List<SingleLegalRestriction>): NewLegalRestriction {
        val document = NewLegalRestrictionDocument(
            id = id,
            restrictions = restrictions.map { SingleLegalRestriction(id = id, text = it.text) }
        )

        getCollection().insertOne(document)

        return find(document.id) ?: throw IllegalStateException("This should never happen")
    }

    override fun findOne(type: String): NewLegalRestriction? {
        return find(type)
    }

    private fun find(id: String): NewLegalRestriction? {
        return getCollection().findOne(NewLegalRestrictionDocument::id eq id)?.toNewLegalRestriction()
    }

    override fun findAll(): List<NewLegalRestriction> {
        return getCollection().find().map { it.toNewLegalRestriction() }.toList()
    }
}