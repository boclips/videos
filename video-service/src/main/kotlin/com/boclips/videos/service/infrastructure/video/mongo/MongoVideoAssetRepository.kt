package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.mongodb.MongoClient
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.ReturnDocument
import org.bson.Document
import org.bson.types.ObjectId
import java.util.*

class MongoVideoAssetRepository(
        private val mongoClient: MongoClient
) : VideoAssetRepository {
    override fun find(assetId: AssetId): VideoAsset? {
        return getVideoCollection().find(eq("_id", ObjectId(assetId.value)))
                .firstOrNull()
                ?.let(VideoDocumentConverter::fromDocument)
    }

    override fun findAll(assetIds: List<AssetId>): List<VideoAsset> {
        val videoByAssetId = getVideoCollection()
                .find(`in`("_id", assetIds.map { ObjectId(it.value) }))
                .map(VideoDocumentConverter::fromDocument)
                .toList()
                .map { (it.assetId to it) }
                .toMap()

        return assetIds.mapNotNull { videoByAssetId[it] }
    }

    override fun streamAll(consumer: (Sequence<VideoAsset>) -> Unit) {
        val sequence = Sequence { getVideoCollection().find().iterator() }
                .map(VideoDocumentConverter::fromDocument)

        consumer(sequence)
    }

    override fun delete(assetId: AssetId) {
        val objectIdToBeDeleted = ObjectId(assetId.value)
        getVideoCollection()
                .deleteOne(eq("_id", objectIdToBeDeleted))
    }

    override fun create(videoAsset: VideoAsset): VideoAsset {
        val id = videoAsset.assetId.copy(value = ObjectId().toHexString())

        val document = VideoDocumentConverter.toDocument(videoAsset.copy(assetId = id))

        getVideoCollection().insertOne(document)

        return find(id) ?: throw VideoAssetNotFoundException()
    }

    fun upsert(videoAsset: VideoAsset): VideoAsset {
        val assetId = migrateToMongoIdIfRequired(videoAsset.assetId)
        val pendingUpdates = VideoDocumentConverter.toDocument(videoAsset.copy(assetId = assetId))

        val upsertedDocument = getVideoCollection().findOneAndReplace(
                findByIdOrAlias(assetId),
                pendingUpdates,
                FindOneAndReplaceOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        )

        return VideoDocumentConverter.fromDocument(upsertedDocument)
    }

    override fun update(videoAsset: VideoAsset): VideoAsset {
        val document = VideoDocumentConverter.toDocument(videoAsset)
        getVideoCollection()
                .replaceOne(eq("_id", ObjectId(videoAsset.assetId.value)), document)

        return find(videoAsset.assetId) ?: throw VideoAssetNotFoundException()
    }

    override fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean {
        val assetMatchingFilters = getVideoCollection()
                .find(and(eq("source.contentPartner.name", contentPartnerId), eq("source.videoReference", partnerVideoId)))
                .first()

        return Optional.ofNullable(assetMatchingFilters).isPresent
    }

    override fun resolveAlias(alias: String): AssetId? {
        return getVideoCollection().find(elemMatch("aliases", Document.parse("{\$eq: \"$alias\"}")))
                .firstOrNull()
                ?.getObjectId("_id")
                ?.toHexString()
                ?.let { AssetId(it, alias) }
    }

    private fun getVideoCollection() = mongoClient.getDatabase("video-service-db").getCollection("videos")

    private fun migrateToMongoIdIfRequired(assetId: AssetId): AssetId {
        val objectId = try {
            ObjectId(assetId.value)
        } catch (e: IllegalArgumentException) {
            ObjectId()
        }

        return AssetId(objectId.toHexString(), assetId.alias)
    }

    private fun findByIdOrAlias(assetId: AssetId) =
            or(eq("_id", ObjectId(assetId.value)), eq("aliases", assetId.alias))
}