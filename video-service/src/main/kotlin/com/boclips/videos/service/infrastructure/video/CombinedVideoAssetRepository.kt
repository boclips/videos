package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.infrastructure.video.mongo.MongoVideoAssetRepository
import com.boclips.videos.service.infrastructure.video.mysql.MysqlVideoAssetRepository
import mu.KLogging
import javax.xml.transform.Templates

class CombinedVideoAssetRepository(
        private val mysqlVideoAssetRepository: MysqlVideoAssetRepository,
        private val mongoVideoAssetRepository: MongoVideoAssetRepository
) : VideoAssetRepository {
    companion object : KLogging()

    override fun find(assetId: AssetId): VideoAsset? {
        return mysqlVideoAssetRepository.find(assetId)
    }

    override fun findAll(assetIds: List<AssetId>): List<VideoAsset> {
        return mysqlVideoAssetRepository.findAll(assetIds)
    }

    override fun streamAll(consumer: (Sequence<VideoAsset>) -> Unit) {
        mysqlVideoAssetRepository.streamAll(consumer)
    }

    override fun delete(assetId: AssetId) {
        mysqlVideoAssetRepository.delete(assetId)
    }

    override fun create(videoAsset: VideoAsset): VideoAsset {
        val mysqlVideoAsset = mysqlVideoAssetRepository.create(videoAsset)
        val mysqlId = mysqlVideoAsset.assetId

        try {
            val mongoVideoAsset = mysqlVideoAsset.copy(assetId = mysqlId.copy(alias = mysqlId.value))
            mongoVideoAssetRepository.create(mongoVideoAsset)
        } catch(e: Exception) {
            logger.error("Error saving video in MongoDB", e)
        }

        return mysqlVideoAsset
    }

    override fun update(videoAsset: VideoAsset): VideoAsset {
        return mysqlVideoAssetRepository.update(videoAsset)
    }

    override fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean {
        return mysqlVideoAssetRepository.existsVideoFromContentPartner(contentPartnerId, partnerVideoId)
    }

    override fun resolveAlias(alias: String): AssetId? {
        return mongoVideoAssetRepository.resolveAlias(alias)
    }

    override fun disableFromSearch(assetIds: List<AssetId>) {
        mysqlVideoAssetRepository.disableFromSearch(assetIds)
    }

    override fun makeSearchable(assetIds: List<AssetId>) {
        mysqlVideoAssetRepository.makeSearchable(assetIds)
    }
}