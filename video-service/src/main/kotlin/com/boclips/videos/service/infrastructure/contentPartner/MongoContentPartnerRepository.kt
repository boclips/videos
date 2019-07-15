package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerFilter
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerUpdateCommand
import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.video.mongo.converters.DistributionMethodDocumentConverter
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.mongodb.MongoClient
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.UpdateOneModel
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.SetTo
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.ne
import org.litote.kmongo.set
import java.time.Instant

class MongoContentPartnerRepository(val mongoClient: MongoClient) : ContentPartnerRepository {

    companion object : KLogging() {
        const val collectionName = "contentPartners"
    }

    override fun create(contentPartner: ContentPartner): ContentPartner {
        val contentPartnerDocument = ContentPartnerDocumentConverter.toContentPartnerDocument(contentPartner)

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
        return findByQuery(toBsonIdFilter(contentPartnerId))
    }

    override fun findByName(contentPartnerName: String): ContentPartner? {
        return findByQuery(ContentPartnerDocument::name eq contentPartnerName)
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
            is ContentPartnerUpdateCommand.ReplaceAgeRange ->
                set(
                    SetTo(
                        ContentPartnerDocument::ageRangeMin,
                        updateCommand.ageRange.min()
                    ),
                    SetTo(
                        ContentPartnerDocument::ageRangeMax,
                        updateCommand.ageRange.max()
                    )
                )
            is ContentPartnerUpdateCommand.ReplaceDistributionMethods ->
                set(
                    ContentPartnerDocument::disabledDistributionMethods,
                    (DistributionMethod.ALL - updateCommand.methods).map(DistributionMethodDocumentConverter::toDocument).toSet()
                )
        }

        return combine(update, set(ContentPartnerDocument::lastModified, Instant.now()))
    }

    private fun filterCommandsToBson(filter: ContentPartnerFilter): Bson =
        when (filter) {
            is ContentPartnerFilter.NameFilter -> ContentPartnerDocument::name eq filter.name
            is ContentPartnerFilter.OfficialFilter -> if (filter.isOfficial) {
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
                    ContentPartnerDocumentConverter.toContentPartner(document)
                }

        contentPartner?.let {
            logger.info { "Found content partner ${it.contentPartnerId.value}" }
        }

        return contentPartner
    }

    private fun getContentPartnerCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<ContentPartnerDocument>(collectionName)

    private fun toBsonIdFilter(contentPartnerId: ContentPartnerId): Bson {
        return ContentPartnerDocument::id eq ObjectId(contentPartnerId.value)
    }
}
