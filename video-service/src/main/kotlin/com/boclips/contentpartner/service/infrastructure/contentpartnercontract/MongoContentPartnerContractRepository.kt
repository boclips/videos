package com.boclips.contentpartner.service.infrastructure.contentpartnercontract

import com.boclips.contentpartner.service.common.PageInfo
import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
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

        logger.info { "Created contentPartnerContract ${created.id.value}" }

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
            logger.info { "Found content partner contract ${it.id.value}" }
        }

        return document
    }

    override fun findAll(pageRequest: PageRequest): ResultsPage<ContentPartnerContract> {
        val count = getCollection().countDocuments()

        return getCollection().find()
            .limit(pageRequest.size)
            .skip(pageRequest.size * pageRequest.page)
            .let {
                ResultsPage(
                    elements = it.map(converter::toContract).toMutableList(),
                    pageInfo = PageInfo(
                        hasMoreElements = count > pageRequest.size * (pageRequest.page + 1),
                        totalElements = count,
                        pageRequest = pageRequest.copy(size = it.count())
                    )
                )
            }
    }

    private fun getCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<ContentPartnerContractDocument>(
            collectionName
        )
}
