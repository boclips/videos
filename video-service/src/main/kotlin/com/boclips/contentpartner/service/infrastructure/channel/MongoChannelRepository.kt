package com.boclips.contentpartner.service.infrastructure.channel

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelFilter
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.contentpartner.service.infrastructure.agerange.AgeRangeDocumentConverter
import com.boclips.contentpartner.service.infrastructure.channel.converters.ChannelDocumentConverter
import com.boclips.contentpartner.service.infrastructure.channel.converters.DistributionMethodDocumentConverter
import com.boclips.contentpartner.service.infrastructure.channel.converters.IngestDetailsDocumentConverter
import com.boclips.contentpartner.service.infrastructure.contract.ContractDocument
import com.boclips.contentpartner.service.infrastructure.contract.ContractDocumentConverter
import com.boclips.contentpartner.service.infrastructure.legalrestriction.LegalRestrictionsDocument
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
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
import org.litote.kmongo.regex
import org.litote.kmongo.set
import java.time.Instant
import java.util.regex.Pattern

class MongoChannelRepository(val mongoClient: MongoClient) :
    ChannelRepository {

    companion object : KLogging() {
        const val collectionName = "channels"
    }

    override fun create(channel: Channel): Channel {
        val channelDocument =
            ChannelDocumentConverter.toChannelDocument(
                channel
            )

        getChannelCollection()
            .insertOne(channelDocument.copy(createdAt = Instant.now(), lastModified = Instant.now()))

        val createChannel = findById(channel.id)
            ?: throw ResourceNotFoundApiException( // TODO this should not be throwing api exceptions
                error = "Content partner not found",
                message = "There has been an error in creating the content partner. Content partner id: ${channel.id.value} could not be found."
            )

        logger.info { "Created channel ${createChannel.id.value}" }

        return createChannel
    }

    override fun findAll(): MongoIterable<Channel> =
        getChannelCollection().find()
            .map { ChannelDocumentConverter.toChannel(it) }

    override fun findAllByIds(ids: List<ChannelId>): List<Channel> {
        val uniqueChannelIds = ids.distinct()
        val uniqueObjectIds = uniqueChannelIds.map { it.value }.distinct().map { ObjectId(it) }

        val channels = getChannelCollection()
            .find(ChannelDocument::id `in` uniqueObjectIds)
            .map(ChannelDocumentConverter::toChannel)
            .map { it.id to it }
            .toMap()

        return uniqueChannelIds.mapNotNull { channelId -> channels[channelId] }
    }

    override fun findAll(filters: List<ChannelFilter>): Iterable<Channel> {
        val bson: Bson = filters.fold(and()) { bson: Bson, filter: ChannelFilter ->
            and(bson, filterCommandsToBson(filter))
        }

        return getChannelCollection().find(bson)
            .map { ChannelDocumentConverter.toChannel(it) }
    }

    override fun findById(channelId: ChannelId): Channel? {
        if (!ObjectId.isValid(channelId.value)) {
            return null
        }

        return findByQuery(toBsonIdFilter(channelId))
    }

    override fun findByContractId(contractId: ContractId): List<Channel> {
        return getChannelCollection()
            .find(
                (ChannelDocument::contract / ContractDocument::id) eq
                    ObjectId(contractId.value)
            )
            .map { ChannelDocumentConverter.toChannel(it) }
            .toList()
    }

    override fun findByName(query: String): List<Channel> {
        return getChannelCollection().find(
            ChannelDocument::name regex Regex(Pattern.quote(query), RegexOption.IGNORE_CASE)
        )
            .distinctBy(selector = { input -> input.name })
            .map { ChannelDocumentConverter.toChannel(it) }
            .toList()
    }

    override fun streamAll(consumer: (Sequence<ChannelSuggestion>) -> Unit) {
        val sequence = Sequence {
            getChannelCollection().find().iterator()
        }.mapNotNull { ChannelDocumentConverter.toChannel(it) }.map {
            ChannelSuggestion(
                name = it.name,
                id = it.id,
                eligibleForStream = it.distributionMethods.contains(DistributionMethod.STREAM),
                contentTypes = it.contentTypes ?: emptyList(),
                taxonomy = it.taxonomy
            )
        }

        consumer(sequence)
    }

    override fun update(updateCommands: List<ChannelUpdateCommand>): List<Channel> {
        if (updateCommands.isEmpty()) {
            return emptyList()
        }

        val updateDocs = updateCommands.map { updateCommand ->
            UpdateOneModel<ChannelDocument>(
                toBsonIdFilter(updateCommand.channelId),
                updateCommandsToBson(updateCommand)
            )
        }

        val result = getChannelCollection().bulkWrite(updateDocs)
        logger.info { "Bulk content partner update: $result" }

        return findAllByIds(updateCommands.map { it.channelId })
    }

    private fun updateCommandsToBson(updateCommand: ChannelUpdateCommand): Bson {
        val update = when (updateCommand) {
            is ChannelUpdateCommand.ReplaceName -> set(ChannelDocument::name, updateCommand.name)
            is ChannelUpdateCommand.ReplaceAgeRanges ->
                set(
                    ChannelDocument::ageRanges,
                    updateCommand.ageRangeBuckets.ageRanges.map { AgeRangeDocumentConverter.toAgeRangeDocument(it) }
                )
            is ChannelUpdateCommand.ReplaceDistributionMethods ->
                set(
                    ChannelDocument::distributionMethods,
                    updateCommand.distributionMethods.map(DistributionMethodDocumentConverter::toDocument).toSet()
                )
            is ChannelUpdateCommand.ReplaceLegalRestrictions ->
                set(
                    ChannelDocument::legalRestrictions,
                    LegalRestrictionsDocument.from(updateCommand.legalRestriction)
                )
            is ChannelUpdateCommand.ReplaceCurrency -> set(
                ChannelDocument::remittanceCurrency,
                updateCommand.currency.currencyCode
            )
            is ChannelUpdateCommand.ReplaceContentTypes -> set(
                ChannelDocument::contentTypes,
                updateCommand.contentType
            )
            is ChannelUpdateCommand.ReplaceContentCategories -> set(
                ChannelDocument::contentCategories,
                updateCommand.contentCategories
            )
            is ChannelUpdateCommand.ReplaceLanguage -> set(
                ChannelDocument::language,
                updateCommand.language
            )
            is ChannelUpdateCommand.ReplaceDescription -> set(
                ChannelDocument::description,
                updateCommand.description
            )
            is ChannelUpdateCommand.ReplaceNotes -> set(
                ChannelDocument::notes,
                updateCommand.notes
            )
            is ChannelUpdateCommand.ReplaceMarketingStatus -> set(
                ChannelDocument::marketingInformation / MarketingInformationDocument::status,
                updateCommand.status
            )
            is ChannelUpdateCommand.ReplaceMarketingLogos -> set(
                ChannelDocument::marketingInformation / MarketingInformationDocument::logos,
                updateCommand.logos.map { it.toString() }
            )
            is ChannelUpdateCommand.ReplaceMarketingShowreel -> set(
                ChannelDocument::marketingInformation / MarketingInformationDocument::showreel,
                updateCommand.showreel.toString()
            )
            is ChannelUpdateCommand.ReplaceMarketingSampleVideos -> set(
                ChannelDocument::marketingInformation / MarketingInformationDocument::sampleVideos,
                updateCommand.sampleVideos.map { it.toString() }
            )
            is ChannelUpdateCommand.ReplaceOneLineDescription -> set(
                ChannelDocument::marketingInformation / MarketingInformationDocument::oneLineDescription,
                updateCommand.oneLineDescription
            )
            is ChannelUpdateCommand.ReplaceBestForTags -> set(
                ChannelDocument::bestForTags,
                updateCommand.bestForTags
            )
            is ChannelUpdateCommand.ReplaceSubjects -> set(
                ChannelDocument::subjects,
                updateCommand.subjects
            )
            is ChannelUpdateCommand.ReplaceIngestDetails -> set(
                ChannelDocument::ingest,
                IngestDetailsDocumentConverter.toIngestDetailsDocument(updateCommand.ingest)
            )
            is ChannelUpdateCommand.ReplaceContract -> set(
                ChannelDocument::contract,
                ContractDocumentConverter().toDocument(updateCommand.contract)
            )
            is ChannelUpdateCommand.ReplaceCategories -> set(
                ChannelDocument::taxonomy / TaxonomyDocument::categories,
                updateCommand.categories.map { CategoriesDocumentConverter.toDocument(it) }
            )
            is ChannelUpdateCommand.ReplaceRequiresVideoLevelTagging ->
                set(
                    ChannelDocument::taxonomy / TaxonomyDocument::requiresVideoLevelTagging,
                    updateCommand.requiresVideoLevelTagging
                )
        }

        return combine(update, set(ChannelDocument::lastModified, Instant.now()))
    }

    private fun filterCommandsToBson(filter: ChannelFilter): Bson =
        when (filter) {
            is ChannelFilter.NameFilter -> ChannelDocument::name eq filter.name
            is ChannelFilter.IngestTypesFilter ->
                ChannelDocument::ingest / IngestDetailsDocument::type `in` filter.ingestTypes.map { it.name }
        }

    private fun findByQuery(mongoQuery: Bson): Channel? {
        val channel =
            getChannelCollection().findOne(mongoQuery)
                ?.let { document: ChannelDocument ->
                    ChannelDocumentConverter.toChannel(
                        document
                    )
                }

        return channel
    }

    private fun getChannelCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<ChannelDocument>(
            collectionName
        )

    private fun toBsonIdFilter(channelId: ChannelId): Bson {
        return ChannelDocument::id eq ObjectId(channelId.value)
    }
}
