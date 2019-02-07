package com.boclips.videos.service.domain.model.asset

import com.boclips.videos.service.domain.service.VideoUpdateIntent

interface VideoAssetRepository {
    fun find(assetId: AssetId): VideoAsset?
    fun findAll(assetIds: List<AssetId>): List<VideoAsset>
    fun streamAllSearchable(consumer: (Sequence<VideoAsset>) -> Unit)
    fun delete(assetId: AssetId)
    fun create(videoAsset: VideoAsset): VideoAsset
    fun replaceSubjects(assetId: AssetId, videoUpdateIntents: List<Subject>): VideoAsset
    fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean
    fun resolveId(assetId: AssetId): String?
    fun resolveAlias(alias: String): AssetId?
    fun disableFromSearch(assetIds: List<AssetId>)
    fun makeSearchable(assetIds: List<AssetId>)
}