package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.collection.CollectionDocument
import com.mongodb.MongoClient
import mu.KLogging
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class MongoContentPartnerRepository(val mongoClient: MongoClient) : ContentPartnerRepository {

    companion object : KLogging() {
        const val collectionName = "contentPartner"
    }

    override fun find(contentPartnerId: ContentPartnerId): ContentPartner? {
        val contentPartner =
            getContentPartnerCollection().findOne(CollectionDocument::id eq ObjectId(contentPartnerId.value))
                ?.let { document: ContentPartnerDocument ->
                    ContentPartnerDocumentConverter.toContentPartner(document)
                }

        contentPartner?.let {
            logger.info { "Found content partner ${it.contentPartnerId.value}" }
        }

        return contentPartner
    }

    override fun create(contentPartner: ContentPartner): ContentPartner {
        getContentPartnerCollection().insertOne(ContentPartnerDocumentConverter.toContentPartnerDocument(contentPartner))

        val createdContentPartner = find(contentPartner.contentPartnerId) ?: throw Exception()

        logger.info { "Created contentPartner ${createdContentPartner.contentPartnerId.value}" }

        return createdContentPartner
    }

    private fun getContentPartnerCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<ContentPartnerDocument>(MongoContentPartnerRepository.collectionName)
}