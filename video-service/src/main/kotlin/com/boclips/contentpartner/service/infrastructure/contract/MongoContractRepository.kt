package com.boclips.contentpartner.service.infrastructure.contract

import com.boclips.contentpartner.service.common.PageInfo
import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand
import com.boclips.contentpartner.service.domain.model.contract.ContractFilter
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.mongodb.MongoClient
import com.mongodb.client.model.UpdateOneModel
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.`in`
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.set
import java.time.Instant

class MongoContractRepository(
    private val mongoClient: MongoClient,
    private val converter: ContractDocumentConverter
) : ContractRepository {
    companion object : KLogging() {
        const val collectionName = "contracts"
    }

    override fun create(contract: Contract): Contract {
        val document = converter.toDocument(contract)
        getCollection().insertOne(document.copy(createdAt = Instant.now(), lastModified = Instant.now()))
        val createdContract = findById(contract.id) ?: throw ResourceNotFoundApiException( // TODO don't throw an api exception here
            error = "Contract not found",
            message = arrayOf(
                "There has been an error in creating the contract.",
                "Contract id: ${contract.id.value} could not be found."
            ).joinToString(separator = " ")
        )

        logger.info { "Created contract ${createdContract.id.value}" }

        return createdContract
    }

    override fun findById(id: ContractId): Contract? {
        if (!ObjectId.isValid(id.value)) {
            return null
        }

        val document =
            getCollection().findOne(ContractDocument::id eq ObjectId(id.value))
                ?.let { document: ContractDocument ->
                    converter.toContract(document)
                }

        document?.let {
            logger.info { "Found content partner contract ${it.id.value}" }
        }

        return document
    }

    override fun findAllByIds(contractIds: List<ContractId>): List<Contract> {
        val uniqueContractIds = contractIds.distinct()
        val uniqueObjectIds = uniqueContractIds.map { ObjectId(it.value) }

        val contracts = getCollection()
            .find(ContractDocument::id `in` uniqueObjectIds)
            .map { document: ContractDocument ->
                converter.toContract(document)
            }
            .map { it.id to it }
            .toMap()

        return uniqueContractIds.mapNotNull { contractId -> contracts[contractId] }
    }

    override fun findAll(pageRequest: PageRequest): ResultsPage<Contract> {
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

    override fun findAll(filters: List<ContractFilter>): Iterable<Contract> {
        val bson = filters.fold(and()) { bson: Bson, filter: ContractFilter ->
            and(
                bson, when (filter) {
                    is ContractFilter.NameFilter -> ContractDocument::contentPartnerName eq filter.name
                }
            )
        }

        return getCollection().find(bson).map(converter::toContract)
    }

    override fun streamAll(consumer: (Sequence<Contract>) -> Unit) {
        val sequence = Sequence { getCollection().find().iterator() }
            .map(converter::toContract)

        consumer(sequence)
    }

    override fun update(contractUpdateCommands: List<ContractUpdateCommand>): List<Contract> {
        if (contractUpdateCommands.isEmpty()) {
            return emptyList()
        }

        val updateDocs = contractUpdateCommands.map { updateCommand ->
            UpdateOneModel<ContractDocument>(
                toBsonIdFilter(updateCommand.contractId),
                updateCommandsToBson(updateCommand)
            )
        }

        val result = getCollection().bulkWrite(updateDocs)
        logger.info { "Bulk contract content partner update: $result" }

        val commandsById = contractUpdateCommands.groupBy { it.contractId }

        return findAllByIds(commandsById.keys.toList())
    }

    private fun updateCommandsToBson(updateCommand: ContractUpdateCommand): Bson {
        val update = when (updateCommand) {
            is ContractUpdateCommand.ReplaceContentPartnerName ->
                set(ContractDocument::contentPartnerName, updateCommand.contentPartnerName)

            is ContractUpdateCommand.ReplaceContractDocument ->
                set(
                    ContractDocument::contractDocument,
                    updateCommand.contractDocument
                )

            is ContractUpdateCommand.ReplaceContractIsRolling ->
                set(ContractDocument::contractIsRolling, updateCommand.contractIsRolling)

            is ContractUpdateCommand.ReplaceContractDates ->
                set(
                    ContractDocument::contractDates,
                    updateCommand.contractDates.let { ContractDatesDocument(it.start.toString(), it.end.toString()) }
                )

            is ContractUpdateCommand.ReplaceDaysBeforeTerminationWarning ->
                set(
                    ContractDocument::daysBeforeTerminationWarning,
                    updateCommand.daysBeforeTerminationWarning
                )

            is ContractUpdateCommand.ReplaceYearsForMaximumLicense ->
                set(ContractDocument::yearsForMaximumLicense, updateCommand.yearsForMaximumLicense)

            is ContractUpdateCommand.ReplaceDaysForSellOffPeriod ->
                set(ContractDocument::daysForSellOffPeriod, updateCommand.daysForSellOffPeriod)

            is ContractUpdateCommand.ReplaceRoyaltySplit ->
                set(ContractDocument::royaltySplit, updateCommand.royaltySplit.let {
                    ContractRoyaltySplitDocument(
                        it.download, it.streaming
                    )
                })

            is ContractUpdateCommand.ReplaceMinimumPriceDescription ->
                set(ContractDocument::minimumPriceDescription, updateCommand.minimumPriceDescription)

            is ContractUpdateCommand.ReplaceRemittanceCurrency ->
                set(ContractDocument::remittanceCurrency, updateCommand.remittanceCurrency)

            is ContractUpdateCommand.ReplaceRestrictions ->
                set(ContractDocument::restrictions, updateCommand.restrictions.let {
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

            is ContractUpdateCommand.ReplaceCost ->
                set(ContractDocument::costs, updateCommand.costs.let {
                    ContractCostsDocument(
                        minimumGuarantee = it.minimumGuarantee,
                        upfrontLicense = it.upfrontLicense,
                        technicalFee = it.technicalFee,
                        recoupable = it.recoupable
                    )
                })

        }

        return combine(update, set(ContractDocument::lastModified, Instant.now()))
    }

    private fun toBsonIdFilter(contractId: ContractId): Bson {
        return ContractDocument::id eq ObjectId(contractId.value)
    }

    private fun getCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<ContractDocument>(
            collectionName
        )
}
