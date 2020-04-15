package com.boclips.contentpartner.service.infrastructure.contract

import com.boclips.contentpartner.service.common.PageInfo
import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.mongodb.MongoClient
import com.mongodb.client.model.UpdateOneModel
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.combine
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.set
import java.time.Instant

class MongoContentPartnerContractRepository(
    private val mongoClient: MongoClient,
    private val converter: ContentPartnerContractDocumentConverter
) : ContentPartnerContractRepository {
    companion object : KLogging() {
        const val collectionName = "contentPartnerContracts"
    }

    override fun create(contract: ContentPartnerContract): ContentPartnerContract {
        val document = converter.toDocument(contract)
        getCollection().insertOne(document.copy(createdAt = Instant.now(), lastModified = Instant.now()))
        val createdContract = findById(contract.id) ?: throw ResourceNotFoundApiException(
            error = "Content partner contract not found",
            message = arrayOf(
                "There has been an error in creating the content partner contract.",
                "Content partner contract id: ${contract.id.value} could not be found."
            ).joinToString(separator = " ")
        )

        logger.info { "Created contentPartnerContract ${createdContract.id.value}" }

        return createdContract
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
                        pageRequest = pageRequest
                    )
                )
            }
    }

    override fun update(contentPartnerUpdateCommands: List<ContractContentPartnerUpdateCommand>) {
        if (contentPartnerUpdateCommands.isEmpty()) {
            return
        }

        val updateDocs = contentPartnerUpdateCommands.map { updateCommand ->
            UpdateOneModel<ContentPartnerContractDocument>(
                toBsonIdFilter(updateCommand.contractContentPartnerId),
                updateCommandsToBson(updateCommand)
            )
        }

        val result = getCollection().bulkWrite(updateDocs)
        logger.info { "Bulk contract content partner update: $result" }
    }

    private fun updateCommandsToBson(updateCommand: ContractContentPartnerUpdateCommand): Bson {
        val update = when (updateCommand) {
            is ContractContentPartnerUpdateCommand.ReplaceContentPartnerName
            -> set(ContentPartnerContractDocument::contentPartnerName, updateCommand.contentPartnerName)

            is ContractContentPartnerUpdateCommand.ReplaceContractIsRolling
            -> set(ContentPartnerContractDocument::contractIsRolling, updateCommand.contractIsRolling)

            is ContractContentPartnerUpdateCommand.ReplaceContractDates
            -> set(
                ContentPartnerContractDocument::contractDates,
                updateCommand.contractDates.let { ContractDatesDocument(it.start.toString(), it.end.toString()) })

            is ContractContentPartnerUpdateCommand.ReplaceDaysBeforeTerminationWarning
            -> set(
                ContentPartnerContractDocument::daysBeforeTerminationWarning,
                updateCommand.daysBeforeTerminationWarning
            )

            is ContractContentPartnerUpdateCommand.ReplaceYearsForMaximumLicense
            -> set(ContentPartnerContractDocument::yearsForMaximumLicense, updateCommand.yearsForMaximumLicense)

            is ContractContentPartnerUpdateCommand.ReplaceDaysForSellOffPeriod
            -> set(ContentPartnerContractDocument::daysForSellOffPeriod, updateCommand.daysForSellOffPeriod)

            is ContractContentPartnerUpdateCommand.ReplaceRoyaltySplit
            -> set(ContentPartnerContractDocument::royaltySplit, updateCommand.royaltySplit.let {
                ContractRoyaltySplitDocument(
                    it.download, it.streaming
                )
            })

            is ContractContentPartnerUpdateCommand.ReplaceMinimumPriceDescription
            -> set(ContentPartnerContractDocument::minimumPriceDescription, updateCommand.minimumPriceDescription)

            is ContractContentPartnerUpdateCommand.ReplaceRemittanceCurrency
            -> set(ContentPartnerContractDocument::remittanceCurrency, updateCommand.remittanceCurrency)

            is ContractContentPartnerUpdateCommand.ReplaceRestrictions
            -> set(ContentPartnerContractDocument::restrictions, updateCommand.restrictions.let {
                ContractRestrictionsDocument(
                    clientFacing = it.clientFacing,
                    territory = it.territory,
                    licensing = it.licensing,
                    editing = it.editing,
                    marketing = it.marketing,
                    companies = it.companies,
                    payout = it.payout,
                    other = it.other
                )
            })

            is ContractContentPartnerUpdateCommand.ReplaceCost
            -> set(ContentPartnerContractDocument::costs, updateCommand.costs.let {
                ContractCostsDocument(
                    minimumGuarantee = it.minimumGuarantee,
                    upfrontLicense = it.upfrontLicense,
                    technicalFee = it.technicalFee,
                    recoupable = it.recoupable
                )
            })

        }

        return combine(update, set(ContentPartnerContractDocument::lastModified, Instant.now()))
    }

    private fun toBsonIdFilter(contentPartnerContractId: ContentPartnerContractId): Bson {
        return ContentPartnerContractDocument::id eq ObjectId(contentPartnerContractId.value)
    }

    private fun getCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<ContentPartnerContractDocument>(
            collectionName
        )
}
