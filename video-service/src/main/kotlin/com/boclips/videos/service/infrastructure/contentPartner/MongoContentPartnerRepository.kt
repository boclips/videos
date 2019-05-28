package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.mongodb.MongoClient
import com.mongodb.client.model.UpdateOptions
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.updateOne

class MongoContentPartnerRepository(val mongoClient: MongoClient) : ContentPartnerRepository {
    companion object : KLogging() {
        const val collectionName = "contentPartner"
    }

    override fun create(contentPartner: ContentPartner): ContentPartner {
        getContentPartnerCollection().insertOne(ContentPartnerDocumentConverter.toContentPartnerDocument(contentPartner))

        val createdContentPartner = find(contentPartner.contentPartnerId) ?: throw ResourceNotFoundApiException(
            error = "Content partner not found",
            message = "There has been an error in creating the content partner.  Content partner id: ${contentPartner.contentPartnerId.value} could not be found."
        )

        logger.info { "Created contentPartner ${createdContentPartner.contentPartnerId.value}" }

        return createdContentPartner
    }

    override fun find(contentPartnerId: ContentPartnerId): ContentPartner? {
        return findByQuery(ContentPartnerDocument::id eq ObjectId(contentPartnerId.value))
    }

    override fun findByName(contentPartnerName: String): ContentPartner? {
        return findByQuery(ContentPartnerDocument::name eq contentPartnerName)
    }

    override fun update(contentPartner: ContentPartner): ContentPartner {
        getContentPartnerCollection().updateOne(
            ContentPartnerDocument::id eq ObjectId(contentPartner.contentPartnerId.value),
            ContentPartnerDocumentConverter.toContentPartnerDocument(contentPartner),
            UpdateOptions().upsert(true)
        )

        val updatedContentPartner = find(contentPartner.contentPartnerId) ?: throw ResourceNotFoundApiException(
            error = "Content partner not found",
            message = "There has been an error in updating the content partner.  Content partner id: ${contentPartner.contentPartnerId.value} could not be found."
        )

        logger.info { "Updated contentPartner ${updatedContentPartner.contentPartnerId.value}" }

        return updatedContentPartner
    }

    private fun findByQuery(mongoQuery: Bson): ContentPartner? {
        val contentPartner =
            getContentPartnerCollection().findOne(mongoQuery)
                ?.let { document: ContentPartnerDocument ->
                    ContentPartnerDocumentConverter.toContentPartner(document)
                }

        contentPartner?.let {
            logger.info { "Found content partner ${it.contentPartnerId.value}" }
        }

        return contentPartner
    }

    private fun getContentPartnerCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<ContentPartnerDocument>(collectionName)
}