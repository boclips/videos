package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerFilter
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.mongodb.MongoClient
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.UpdateOneModel
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.ne
import org.litote.kmongo.set
import java.time.Instant

class MongoContentPartnerRepository(val mongoClient: MongoClient) :
    ContentPartnerRepository {

    companion object : KLogging() {
        const val collectionName = "contentPartners"
    }

    override fun create(contentPartner: ContentPartner): ContentPartner {
        val contentPartnerDocument =
            ContentPartnerDocumentConverter.toContentPartnerDocument(
                contentPartner
            )

        getContentPartnerCollection()
            .insertOne(contentPartnerDocument.copy(createdAt = Instant.now(), lastModified = Instant.now()))

        val createdContentPartner = findById(contentPartner.contentPartnerId) ?: throw ResourceNotFoundApiException(
            error = "Content partner not found",
            message = "There has been an error in creating the content partner. Content partner id: ${contentPartner.contentPartnerId.value} could not be found."
        )

        logger.info { "Created contentPartner ${createdContentPartner.contentPartnerId.value}" }

        return createdContentPartner
    }

    override fun findAll(): MongoIterable<ContentPartner> =
        getContentPartnerCollection().find()
            .map { ContentPartnerDocumentConverter.toContentPartner(it) }

    override fun findAll(filters: List<ContentPartnerFilter>): Iterable<ContentPartner> {
        val bson: Bson = filters.fold(and()) { bson: Bson, filter: ContentPartnerFilter ->
            and(bson, filterCommandsToBson(filter))
        }

        return getContentPartnerCollection().find(bson)
            .map { ContentPartnerDocumentConverter.toContentPartner(it) }
    }

    override fun findById(contentPartnerId: ContentPartnerId): ContentPartner? {
        if (!ObjectId.isValid(contentPartnerId.value)) {
            return null
        }

        return findByQuery(toBsonIdFilter(contentPartnerId))
    }

    override fun update(updateCommands: List<ContentPartnerUpdateCommand>) {
        if (updateCommands.isEmpty()) {
            return
        }

        val updateDocs = updateCommands.map { updateCommand ->
            UpdateOneModel<ContentPartnerDocument>(
                toBsonIdFilter(updateCommand.contentPartnerId),
                updateCommandsToBson(updateCommand)
            )
        }

        val result = getContentPartnerCollection().bulkWrite(updateDocs)
        logger.info { "Bulk content partner update: $result" }
    }

    private fun updateCommandsToBson(updateCommand: ContentPartnerUpdateCommand): Bson {
        val update = when (updateCommand) {
            is ContentPartnerUpdateCommand.ReplaceName -> set(ContentPartnerDocument::name, updateCommand.name)
            is ContentPartnerUpdateCommand.ReplaceAgeRanges ->
                set(
                    ContentPartnerDocument::ageRanges,
                    updateCommand.ageRangeBuckets.ageRanges.map { AgeRangeDocumentConverter.toAgeRangeDocument(it) }
                )
            is ContentPartnerUpdateCommand.ReplaceDistributionMethods ->
                set(
                    ContentPartnerDocument::distributionMethods,
                    updateCommand.distributionMethods.map(DistributionMethodDocumentConverter::toDocument).toSet()
                )
            is ContentPartnerUpdateCommand.ReplaceLegalRestrictions ->
                set(
                    ContentPartnerDocument::legalRestrictions,
                    LegalRestrictionsDocument.from(updateCommand.legalRestriction)
                )
            is ContentPartnerUpdateCommand.ReplaceCurrency -> set(
                ContentPartnerDocument::remittanceCurrency,
                updateCommand.currency.currencyCode
            )
            is ContentPartnerUpdateCommand.ReplaceContentTypes -> set(
                ContentPartnerDocument::contentTypes,
                updateCommand.contentType
            )
            is ContentPartnerUpdateCommand.ReplaceContentCategories -> set(
                ContentPartnerDocument::contentCategories,
                updateCommand.contentCategories
            )
            is ContentPartnerUpdateCommand.ReplaceLanguage -> set(
                ContentPartnerDocument::language,
                updateCommand.language
            )
            is ContentPartnerUpdateCommand.ReplaceDescription -> set(
                ContentPartnerDocument::description,
                updateCommand.description
            )
            is ContentPartnerUpdateCommand.ReplaceAwards -> set(
                ContentPartnerDocument::awards,
                updateCommand.awards
            )
            is ContentPartnerUpdateCommand.ReplaceHubspotId -> set(
                ContentPartnerDocument::hubspotId,
                updateCommand.hubspotId
            )
            is ContentPartnerUpdateCommand.ReplaceNotes -> set(
                ContentPartnerDocument::notes,
                updateCommand.notes
            )
            is ContentPartnerUpdateCommand.ReplaceMarketingStatus -> set(
                ContentPartnerDocument::marketingInformation / MarketingInformationDocument::status,
                updateCommand.status
            )
            is ContentPartnerUpdateCommand.ReplaceMarketingLogos -> set(
                ContentPartnerDocument::marketingInformation / MarketingInformationDocument::logos,
                updateCommand.logos.map {it.toString()}
            )
            is ContentPartnerUpdateCommand.ReplaceMarketingShowreel -> set(
                ContentPartnerDocument::marketingInformation / MarketingInformationDocument::showreel,
                updateCommand.showreel.toString()
            )
            is ContentPartnerUpdateCommand.ReplaceMarketingSampleVideos -> set(
                ContentPartnerDocument::marketingInformation / MarketingInformationDocument::sampleVideos,
                updateCommand.sampleVideos.map {it.toString()}
            )
            is ContentPartnerUpdateCommand.ReplaceOneLineDescription -> set(
                ContentPartnerDocument::marketingInformation / MarketingInformationDocument::oneLineDescription,
                updateCommand.oneLineDescription
            )
            is ContentPartnerUpdateCommand.ReplaceIsTranscriptProvided -> set(
                ContentPartnerDocument::isTranscriptProvided,
                updateCommand.isTranscriptProvided
            )
            is ContentPartnerUpdateCommand.ReplaceEducationalResources -> set(
                ContentPartnerDocument::educationalResources,
                updateCommand.educationalResources
            )
            is ContentPartnerUpdateCommand.ReplaceCurriculumAligned -> set(
                ContentPartnerDocument::curriculumAligned,
                updateCommand.curriculumAligned
            )
            is ContentPartnerUpdateCommand.ReplaceBestForTags -> set(
                ContentPartnerDocument::bestForTags,
                updateCommand.bestForTags
            )
            is ContentPartnerUpdateCommand.ReplaceSubjects -> set(
                ContentPartnerDocument::subjects,
                updateCommand.subjects
            )
            is ContentPartnerUpdateCommand.ReplaceIngestDetails -> set(
                ContentPartnerDocument::ingest,
                IngestDetailsDocumentConverter.toIngestDetailsDocument(updateCommand.ingest)
            )
            is ContentPartnerUpdateCommand.ReplaceDeliveryFrequency -> set(
                ContentPartnerDocument::deliveryFrequency,
                updateCommand.deliveryFrequency.toString()
            )
        }

        return combine(update, set(ContentPartnerDocument::lastModified, Instant.now()))
    }

    private fun filterCommandsToBson(filter: ContentPartnerFilter): Bson =
        when (filter) {
            is ContentPartnerFilter.NameFilter -> ContentPartnerDocument::name eq filter.name
            is ContentPartnerFilter.OfficialFilter -> if (filter.official) {
                ContentPartnerDocument::youtubeChannelId eq null
            } else {
                ContentPartnerDocument::youtubeChannelId ne null
            }
            is ContentPartnerFilter.AccreditedTo -> when (filter.credit) {
                is Credit.YoutubeCredit -> ContentPartnerDocument::youtubeChannelId eq filter.credit.channelId
                Credit.PartnerCredit -> ContentPartnerDocument::youtubeChannelId ne null
            }
        }

    private fun findByQuery(mongoQuery: Bson): ContentPartner? {
        val contentPartner =
            getContentPartnerCollection().findOne(mongoQuery)
                ?.let { document: ContentPartnerDocument ->
                    ContentPartnerDocumentConverter.toContentPartner(
                        document
                    )
                }

        contentPartner?.let {
            logger.info { "Found content partner ${it.contentPartnerId.value}" }
        }

        return contentPartner
    }

    private fun getContentPartnerCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<ContentPartnerDocument>(
            collectionName
        )

    private fun toBsonIdFilter(contentPartnerId: ContentPartnerId): Bson {
        return ContentPartnerDocument::id eq ObjectId(contentPartnerId.value)
    }
}
