package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.PartialVideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.mongodb.MongoClient
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.Updates.set
import mu.KLogging
import org.bson.Document
import org.bson.types.ObjectId
import java.util.Optional

class MongoVideoAssetRepository(
    private val mongoClient: MongoClient
) : VideoAssetRepository {
    companion object : KLogging() {
        const val databaseName = "video-service-db"
        const val collectionName = "videos"
    }

    override fun find(assetId: AssetId): VideoAsset? {
        val videoAssetOrNull = getVideoCollection().find(eq("_id", ObjectId(assetId.value)))
            .firstOrNull()
            ?.let(VideoDocumentConverter::fromDocument)

        logger.info { "Found ${assetId.value}" }

        return videoAssetOrNull
    }

    override fun findAll(assetIds: List<AssetId>): List<VideoAsset> {
        val videoByAssetId = getVideoCollection()
            .find(`in`("_id", assetIds.map { ObjectId(it.value) }))
            .map(VideoDocumentConverter::fromDocument)
            .toList()
            .map { (it.assetId to it) }
            .toMap()

        logger.info { "Found ${assetIds.size} videos for assetIds $assetIds" }

        return assetIds.mapNotNull { assetId ->
            videoByAssetId[assetId]
        }
    }

    override fun streamAllSearchable(consumer: (Sequence<VideoAsset>) -> Unit) {
        val sequence = Sequence { getVideoCollection().find(eq("searchable", true)).iterator() }
            .map(VideoDocumentConverter::fromDocument)

        consumer(sequence)
    }

    override fun delete(assetId: AssetId) {
        val objectIdToBeDeleted = ObjectId(assetId.value)
        getVideoCollection()
            .deleteOne(eq("_id", objectIdToBeDeleted))

        logger.info { "Deleted video ${assetId.value}" }
    }

    override fun create(videoAsset: VideoAsset): VideoAsset {
        val document = VideoDocumentConverter.toDocument(videoAsset)

        getVideoCollection().insertOne(document)

        val createdVideoAsset = find(videoAsset.assetId) ?: throw VideoAssetNotFoundException(videoAsset.assetId)

        logger.info { "Created video ${createdVideoAsset.assetId.value}" }
        return createdVideoAsset
    }

    override fun update(assetId: AssetId, attributes: PartialVideoAsset): VideoAsset {
        getVideoCollection().updateOne(
            eq(ObjectId(assetId.value)),
            Document("\$set", VideoDocumentConverter.toPartialDocument(attributes))
        )

        return find(assetId) ?: throw VideoAssetNotFoundException(assetId)
    }

    override fun bulkUpdate(updates: List<Pair<AssetId, PartialVideoAsset>>) {
        val updateDocs = updates.map { (assetId, attributes) ->
            UpdateOneModel<Document>(
                eq(ObjectId(assetId.value)),
                Document("\$set", VideoDocumentConverter.toPartialDocument(attributes))
            )
        }

        val result = getVideoCollection().bulkWrite(updateDocs)
        logger.info("Bulk update: $result")
    }

    override fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean {
        val assetMatchingFilters = getVideoCollection()
            .find(and(eq("source.contentPartner.name", contentPartnerId), eq("source.videoReference", partnerVideoId)))
            .first()

        return Optional.ofNullable(assetMatchingFilters).isPresent
    }

    override fun resolveAlias(alias: String): AssetId? {
        val assetId = getVideoCollection().find(eq("aliases", alias))
            .firstOrNull()
            ?.getObjectId("_id")
            ?.toHexString()
            ?.let { AssetId(it) }

        logger.info { "Attempted to resolve alias $alias to $assetId" }

        return assetId
    }

    override fun disableFromSearch(assetIds: List<AssetId>) {
        val mongoIds = assetIds.map { ObjectId(it.value) }
        getVideoCollection().updateMany(
            `in`("_id", mongoIds),
            set("searchable", false)
        )

        logger.info { "Disabled $assetIds for search" }
    }

    override fun makeSearchable(assetIds: List<AssetId>) {
        val mongoIds = assetIds.map { ObjectId(it.value) }

        getVideoCollection().updateMany(
            `in`("_id", mongoIds),
            set("searchable", true)
        )

        logger.info { "Made $assetIds searchable" }
    }

    private fun getVideoCollection() = mongoClient.getDatabase(databaseName).getCollection(collectionName)
}