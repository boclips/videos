package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository

class VideoAccessService(
        private val videoAssetRepository: VideoAssetRepository
) {
    fun accessible(assetId: AssetId): Boolean {
        val videoAsset = videoAssetRepository.find(assetId)

        videoAsset?.let { return it.searchable } ?: throw VideoAssetNotFoundException()
    }

    fun grantAccess(assetIds: List<AssetId>) {
        videoAssetRepository.bulkUpdate(assetIds.map(VideoUpdateCommand::MakeSearchable))
    }

    fun revokeAccess(assetIds: List<AssetId>) {
        videoAssetRepository.bulkUpdate(assetIds.map(VideoUpdateCommand::HideFromSearch))
    }
}
