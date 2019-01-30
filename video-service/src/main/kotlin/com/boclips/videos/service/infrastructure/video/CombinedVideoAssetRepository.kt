package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.infrastructure.video.mysql.MysqlVideoAssetRepository

public class CombinedVideoAssetRepository(
        private val mysqlVideoAssetRepository: MysqlVideoAssetRepository
) : VideoAssetRepository {


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
        return mysqlVideoAssetRepository.create(videoAsset)
    }

    override fun update(videoAsset: VideoAsset): VideoAsset {
        return mysqlVideoAssetRepository.update(videoAsset)
    }

    override fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean {
        return mysqlVideoAssetRepository.existsVideoFromContentPartner(contentPartnerId, partnerVideoId)
    }
}