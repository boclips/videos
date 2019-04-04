package com.boclips.videos.service.domain.model.asset

import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import java.util.Locale

interface VideoAssetRepository {
    fun find(assetId: AssetId): VideoAsset?
    fun findAll(assetIds: List<AssetId>): List<VideoAsset>
    fun streamAllSearchable(consumer: (Sequence<VideoAsset>) -> Unit)
    fun delete(assetId: AssetId)
    fun create(videoAsset: VideoAsset): VideoAsset
    fun update(command: VideoUpdateCommand): VideoAsset
    fun bulkUpdate(commands: List<VideoUpdateCommand>)
    fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean
    fun resolveAlias(alias: String): AssetId?
    fun setSearchable(assetIds: List<AssetId>, searchable: Boolean)
    fun setLanguage(assetId: AssetId, language: Locale)
    fun setTranscript(assetId: AssetId, transcript: String)
    fun setTopics(assetId: AssetId, topics: Set<Topic>)
}