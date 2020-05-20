package com.boclips.contentpartner.service.infrastructure.contentpartner

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelFilter
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.model.channel.Credit
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.infrastructure.agerange.AgeRangeDocumentConverter
import com.boclips.contentpartner.service.infrastructure.contentpartner.converters.ContentPartnerDocumentConverter
import com.boclips.contentpartner.service.infrastructure.contentpartner.converters.DistributionMethodDocumentConverter
import com.boclips.contentpartner.service.infrastructure.contentpartner.converters.IngestDetailsDocumentConverter
import com.boclips.contentpartner.service.infrastructure.contract.ContentPartnerContractDocument
import com.boclips.contentpartner.service.infrastructure.contract.ContentPartnerContractDocumentConverter
import com.boclips.contentpartner.service.infrastructure.legalrestriction.LegalRestrictionsDocument
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.mongodb.MongoClient
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.UpdateOneModel
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.`in`
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.ne
import org.litote.kmongo.regex
import org.litote.kmongo.set
import java.time.Instant

class MongoContentPartnerRepository(val mongoClient: MongoClient) :
    ChannelRepository {

    companion object : KLogging() {
        const val collectionName = "contentPartners"
    }

    override fun create(channel: Channel): Channel {
        val contentPartnerDocument =
            ContentPartnerDocumentConverter.toContentPartnerDocument(
                channel
            )

        getContentPartnerCollection()
            .insertOne(contentPartnerDocument.copy(createdAt = Instant.now(), lastModified = Instant.now()))

        val createdContentPartner = findById(channel.id) ?: throw ResourceNotFoundApiException(
            error = "Content partner not found",
            message = "There has been an error in creating the content partner. Content partner id: ${channel.id.value} could not be found."
        )

        logger.info { "Created contentPartner ${createdContentPartner.id.value}" }

        return createdContentPartner
    }

    override fun findAll(): MongoIterable<Channel> =
        getContentPartnerCollection().find()
            .map { ContentPartnerDocumentConverter.toContentPartner(it) }

    override fun findAll(filters: List<ChannelFilter>): Iterable<Channel> {
        val bson: Bson = filters.fold(and()) { bson: Bson, filter: ChannelFilter ->
            and(bson, filterCommandsToBson(filter))
        }

        return getContentPartnerCollection().find(bson)
            .map { ContentPartnerDocumentConverter.toContentPartner(it) }
    }

    override fun findById(channelId: ChannelId): Channel? {
        if (!ObjectId.isValid(channelId.value)) {
            return null
        }

        return findByQuery(toBsonIdFilter(channelId))
    }

    override fun findByContractId(contractId: ContentPartnerContractId): List<Channel> {
        return getContentPartnerCollection()
            .find(
                (ContentPartnerDocument::contract / ContentPartnerContractDocument::id) eq
                    ObjectId(contractId.value)
            )
            .map { ContentPartnerDocumentConverter.toContentPartner(it) }
            .toList()
    }

    override fun findByName(query: String): List<Channel> {
        return getContentPartnerCollection().find(
            ContentPartnerDocument::name regex Regex(query, RegexOption.IGNORE_CASE)
        )
            .distinctBy(selector = { input -> input.name })
            .map { ContentPartnerDocumentConverter.toContentPartner(it) }
            .toList()
    }

    override fun update(updateCommands: List<ChannelUpdateCommand>) {
        if (updateCommands.isEmpty()) {
            return
        }

        val updateDocs = updateCommands.map { updateCommand ->
            UpdateOneModel<ContentPartnerDocument>(
                toBsonIdFilter(updateCommand.channelId),
                updateCommandsToBson(updateCommand)
            )
        }

        val result = getContentPartnerCollection().bulkWrite(updateDocs)
        logger.info { "Bulk content partner update: $result" }
    }

    private fun updateCommandsToBson(updateCommand: ChannelUpdateCommand): Bson {
        val update = when (updateCommand) {
            is ChannelUpdateCommand.ReplaceName -> set(ContentPartnerDocument::name, updateCommand.name)
            is ChannelUpdateCommand.ReplaceAgeRanges ->
                set(
                    ContentPartnerDocument::ageRanges,
                    updateCommand.ageRangeBuckets.ageRanges.map { AgeRangeDocumentConverter.toAgeRangeDocument(it) }
                )
            is ChannelUpdateCommand.ReplaceDistributionMethods ->
                set(
                    ContentPartnerDocument::distributionMethods,
                    updateCommand.distributionMethods.map(DistributionMethodDocumentConverter::toDocument).toSet()
                )
            is ChannelUpdateCommand.ReplaceLegalRestrictions ->
                set(
                    ContentPartnerDocument::legalRestrictions,
                    LegalRestrictionsDocument.from(updateCommand.legalRestriction)
                )
            is ChannelUpdateCommand.ReplaceCurrency -> set(
                ContentPartnerDocument::remittanceCurrency,
                updateCommand.currency.currencyCode
            )
            is ChannelUpdateCommand.ReplaceContentTypes -> set(
                ContentPartnerDocument::contentTypes,
                updateCommand.contentType
            )
            is ChannelUpdateCommand.ReplaceContentCategories -> set(
                ContentPartnerDocument::contentCategories,
                updateCommand.contentCategories
            )
            is ChannelUpdateCommand.ReplaceLanguage -> set(
                ContentPartnerDocument::language,
                updateCommand.language
            )
            is ChannelUpdateCommand.ReplaceDescription -> set(
                ContentPartnerDocument::description,
                updateCommand.description
            )
            is ChannelUpdateCommand.ReplaceAwards -> set(
                ContentPartnerDocument::awards,
                updateCommand.awards
            )
            is ChannelUpdateCommand.ReplaceHubspotId -> set(
                ContentPartnerDocument::hubspotId,
                updateCommand.hubspotId
            )
            is ChannelUpdateCommand.ReplaceNotes -> set(
                ContentPartnerDocument::notes,
                updateCommand.notes
            )
            is ChannelUpdateCommand.ReplaceMarketingStatus -> set(
                ContentPartnerDocument::marketingInformation / MarketingInformationDocument::status,
                updateCommand.status
            )
            is ChannelUpdateCommand.ReplaceMarketingLogos -> set(
                ContentPartnerDocument::marketingInformation / MarketingInformationDocument::logos,
                updateCommand.logos.map { it.toString() }
            )
            is ChannelUpdateCommand.ReplaceMarketingShowreel -> set(
                ContentPartnerDocument::marketingInformation / MarketingInformationDocument::showreel,
                updateCommand.showreel.toString()
            )
            is ChannelUpdateCommand.ReplaceMarketingSampleVideos -> set(
                ContentPartnerDocument::marketingInformation / MarketingInformationDocument::sampleVideos,
                updateCommand.sampleVideos.map { it.toString() }
            )
            is ChannelUpdateCommand.ReplaceOneLineDescription -> set(
                ContentPartnerDocument::marketingInformation / MarketingInformationDocument::oneLineDescription,
                updateCommand.oneLineDescription
            )
            is ChannelUpdateCommand.ReplaceIsTranscriptProvided -> set(
                ContentPartnerDocument::isTranscriptProvided,
                updateCommand.isTranscriptProvided
            )
            is ChannelUpdateCommand.ReplaceEducationalResources -> set(
                ContentPartnerDocument::educationalResources,
                updateCommand.educationalResources
            )
            is ChannelUpdateCommand.ReplaceCurriculumAligned -> set(
                ContentPartnerDocument::curriculumAligned,
                updateCommand.curriculumAligned
            )
            is ChannelUpdateCommand.ReplaceBestForTags -> set(
                ContentPartnerDocument::bestForTags,
                updateCommand.bestForTags
            )
            is ChannelUpdateCommand.ReplaceSubjects -> set(
                ContentPartnerDocument::subjects,
                updateCommand.subjects
            )
            is ChannelUpdateCommand.ReplaceIngestDetails -> set(
                ContentPartnerDocument::ingest,
                IngestDetailsDocumentConverter.toIngestDetailsDocument(updateCommand.ingest)
            )
            is ChannelUpdateCommand.ReplaceDeliveryFrequency -> set(
                ContentPartnerDocument::deliveryFrequency,
                updateCommand.deliveryFrequency.toString()
            )
            is ChannelUpdateCommand.ReplaceContract -> set(
                ContentPartnerDocument::contract,
                ContentPartnerContractDocumentConverter().toDocument(updateCommand.contract)
            )
        }

        return combine(update, set(ContentPartnerDocument::lastModified, Instant.now()))
    }

    private fun filterCommandsToBson(filter: ChannelFilter): Bson =
        when (filter) {
            is ChannelFilter.NameFilter -> ContentPartnerDocument::name eq filter.name
            is ChannelFilter.OfficialFilter -> if (filter.official) {
                ContentPartnerDocument::youtubeChannelId eq null
            } else {
                ContentPartnerDocument::youtubeChannelId ne null
            }
            is ChannelFilter.AccreditedTo -> when (filter.credit) {
                is Credit.YoutubeCredit -> ContentPartnerDocument::youtubeChannelId eq filter.credit.channelId
                Credit.PartnerCredit -> ContentPartnerDocument::youtubeChannelId ne null
            }
            is ChannelFilter.HubspotIdFilter -> ContentPartnerDocument::hubspotId eq filter.hubspotId
            is ChannelFilter.IngestTypesFilter ->
                ContentPartnerDocument::ingest / IngestDetailsDocument::type `in` filter.ingestTypes.map { it.name }
        }

    private fun findByQuery(mongoQuery: Bson): Channel? {
        val contentPartner =
            getContentPartnerCollection().findOne(mongoQuery)
                ?.let { document: ContentPartnerDocument ->
                    ContentPartnerDocumentConverter.toContentPartner(
                        document
                    )
                }
        
        return contentPartner
    }

    private fun getContentPartnerCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<ContentPartnerDocument>(
            collectionName
        )

    private fun toBsonIdFilter(channelId: ChannelId): Bson {
        return ContentPartnerDocument::id eq ObjectId(channelId.value)
    }
}
