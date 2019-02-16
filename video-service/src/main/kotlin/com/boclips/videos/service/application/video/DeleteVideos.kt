package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.service.video.SearchService
import com.boclips.videos.service.domain.service.video.VideoService

class DeleteVideos(
    private val videoAssetRepository: VideoAssetRepository,
    private val searchService: SearchService,
    private val playbackRepository: PlaybackRepository
) {
    operator fun invoke(id: String?) {
        if (id == null || id.isBlank()) {
            throw VideoAssetNotFoundException()
        }

        val assetId = AssetId(value = id)
        val videoAsset = videoAssetRepository.find(assetId) ?: throw VideoAssetNotFoundException(assetId)

        removeVideo(videoAsset)
    }

    private fun removeVideo(videoAsset: VideoAsset) {
        val assetIdToBeDeleted = videoAsset.assetId

        searchService.removeFromSearch(assetIdToBeDeleted.value)
        VideoService.logger.info { "Removed asset $assetIdToBeDeleted from search index" }

        videoAssetRepository.delete(assetIdToBeDeleted)
        VideoService.logger.info { "Removed asset $assetIdToBeDeleted from asset repository" }

        playbackRepository.remove(videoAsset.playbackId)
        VideoService.logger.info { "Removed asset $assetIdToBeDeleted from asset host" }
    }
}