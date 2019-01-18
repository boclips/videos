package com.boclips.videos.service.domain.service

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.model.VideoUpdateCommand
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRespository
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging

class VideoService(
        private val videoAssetRepository: VideoAssetRepository,
        private val searchService: SearchService,
        private val playbackRepository: PlaybackRespository
) {
    companion object : KLogging()

    fun search(query: VideoSearchQuery): List<Video> {
        val searchRequest = PaginatedSearchRequest(
                query = query.toSearchQuery(),
                startIndex = convertPageToIndex(query.pageSize, query.pageIndex),
                windowSize = query.pageSize
        )
        val videoIds = searchService.search(searchRequest).map { AssetId(value = it) }

        val allVideoAssets = videoAssetRepository.findAll(videoIds)
        val videoPlaybacks = playbackRepository.find(allVideoAssets.map { it.playbackId })

        if (videoIds.size != videoPlaybacks.size) {
            logger.warn { "Found ${videoIds.size} videos with ${videoPlaybacks.size} playbacks for query ${query.text}" }
        }

        return allVideoAssets.mapNotNull { videoAsset ->
            val videoPlayback = videoPlaybacks[videoAsset.playbackId] ?: return@mapNotNull null
            Video(videoAsset, videoPlayback)
        }
    }

    fun count(videoSearchQuery: VideoSearchQuery): Long {
        return searchService.count(videoSearchQuery.toSearchQuery())
    }

    fun get(assetId: AssetId): Video {
        val videoAsset = videoAssetRepository
                .find(assetId) ?: throw VideoAssetNotFoundException()

        val videoPlayback = playbackRepository
                .find(videoAsset.playbackId) ?: throw VideoPlaybackNotFound()

        return Video(videoAsset, videoPlayback)
    }

    fun get(assetIds: List<AssetId>): List<Video> {
        return assetIds.mapNotNull { assetId -> this.get(assetId) }
    }

    fun update(assetId: AssetId, updateCommand: VideoUpdateCommand): Video {
        val video = get(assetId)
        val updatedVideo = updateCommand.update(video)
        val savedVideoAsset = videoAssetRepository.update(updatedVideo.asset)
        return updatedVideo.copy(asset = savedVideoAsset)
    }
}

