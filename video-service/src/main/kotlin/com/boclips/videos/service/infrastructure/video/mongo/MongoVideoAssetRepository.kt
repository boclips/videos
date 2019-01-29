package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.mongodb.MongoClient
import com.mongodb.client.model.Filters.eq
import org.bson.types.ObjectId

class MongoVideoAssetRepository(
        private val mongoClient: MongoClient
) : VideoAssetRepository {

    override fun find(assetId: AssetId): VideoAsset? {
        val document = getVideoCollection().find(eq("_id", ObjectId(assetId.value))).first()
        return VideoDocumentConverter.fromDocument(document)
    }

    override fun findAll(assetIds: List<AssetId>): List<VideoAsset> {
        TODO("not implemented")
    }

    override fun streamAll(consumer: (Sequence<VideoAsset>) -> Unit) {
        TODO("not implemented")
    }

    override fun delete(assetId: AssetId) {
        TODO("not implemented")
    }

    override fun create(videoAsset: VideoAsset): VideoAsset {
        getVideoCollection().insertOne(VideoDocumentConverter.toDocument(videoAsset))

        return videoAsset
    }

    override fun update(videoAsset: VideoAsset): VideoAsset {
        TODO("not implemented")
    }

    override fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean {
        TODO("not implemented")
    }

    private fun getVideoCollection() = mongoClient.getDatabase("video-service-db").getCollection("videos")
}