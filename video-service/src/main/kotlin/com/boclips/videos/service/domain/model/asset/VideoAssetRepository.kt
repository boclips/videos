package com.boclips.videos.service.domain.model.asset

interface VideoAssetRepository {
    fun find(assetId: AssetId): VideoAsset?
    fun findAll(assetIds: List<AssetId>): List<VideoAsset>
    fun streamAll(consumer: (Sequence<VideoAsset>) -> Unit)
    fun delete(assetId: AssetId)
    fun create(videoAsset: VideoAsset): VideoAsset
    fun update(videoAsset: VideoAsset): VideoAsset
    fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean
    fun resolveAlias(alias: String): AssetId?
    fun disableFromSearch(assetIds: List<AssetId>)
    fun makeSearchable(assetIds: List<AssetId>)
}