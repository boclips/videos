package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.ContentPartnerContractRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.mongodb.MongoClient
import mu.KLogging
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import java.time.Instant

class MongoContentPartnerContractRepository(
    private val mongoClient: MongoClient,
    private val converter: ContentPartnerContractDocumentConverter
) : ContentPartnerContractRepository {
    companion object : KLogging() {
        const val collectionName = "contentPartnerContracts"
    }

    override fun create(contract: ContentPartnerContract): ContentPartnerContractId {
        val document = converter.toDocument(contract)
        getCollection().insertOne(document.copy(createdAt = Instant.now(), lastModified = Instant.now()))
        val created = findById(contract.id) ?: throw ResourceNotFoundApiException(
            error = "Content partner contract not found",
            message = arrayOf(
                "There has been an error in creating the content partner contract.",
                "Content partner contract id: ${contract.id.value} could not be found."
            ).joinToString(separator = " ")
        )

        MongoContentPartnerRepository.logger.info { "Created contentPartnerContract ${created.id.value}" }

        return created.id
    }

    override fun findById(id: ContentPartnerContractId): ContentPartnerContract? {
        if (!ObjectId.isValid(id.value)) {
            return null
        }

        val document =
            getCollection().findOne(ContentPartnerContractDocument::id eq ObjectId(id.value))
                ?.let { document: ContentPartnerContractDocument ->
                    converter.toContract(document)
                }

        document?.let {
            MongoContentPartnerRepository.logger.info { "Found content partner ${it.id.value}" }
        }

        return document
    }

    private fun getCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<ContentPartnerContractDocument>(
            MongoContentPartnerRepository.collectionName
        )
}