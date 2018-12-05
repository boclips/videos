package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRespository
import com.boclips.videos.service.domain.service.VideoService

class DeleteVideos(
        private val videoAssetRepository: VideoAssetRepository,
        private val searchService: GenericSearchService<VideoAsset>,
        private val playbackRepository: PlaybackRespository
) {
    fun execute(id: String?) {
        if (id == null || id.isBlank()) {
            throw VideoAssetNotFoundException()
        }

        val videoAsset = videoAssetRepository.find(AssetId(value = id)) ?: throw VideoAssetNotFoundException()

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