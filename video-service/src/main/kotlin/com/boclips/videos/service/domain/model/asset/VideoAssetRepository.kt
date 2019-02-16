package com.boclips.videos.service.domain.model.asset

import com.boclips.videos.service.domain.service.video.VideoUpdateCommand

interface VideoAssetRepository {
    fun find(assetId: AssetId): VideoAsset?
    fun findAll(assetIds: List<AssetId>): List<VideoAsset>
    fun streamAllSearchable(consumer: (Sequence<VideoAsset>) -> Unit)
    fun delete(assetId: AssetId)
    fun create(videoAsset: VideoAsset): VideoAsset
    fun update(udpate: VideoUpdateCommand): VideoAsset
    fun bulkUpdate(updates: List<VideoUpdateCommand>)
    fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean
    fun resolveAlias(alias: String): AssetId?
    fun disableFromSearch(assetIds: List<AssetId>)
    fun makeSearchable(assetIds: List<AssetId>)
}