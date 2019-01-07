package com.boclips.videos.service.domain.service

import com.boclips.search.service.domain.Filter
import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.Query
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.*
import com.boclips.videos.service.domain.model.asset.AssetId
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
        val filters = query.filters.map { when (it) {
            VideoSearchQueryFilter.EDUCATIONAL -> Filter(VideoMetadata::isEducational, true)
        }}

        val searchRequest = PaginatedSearchRequest(
                query = Query.parse(query.text).withFilters(filters),
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
        val filters = videoSearchQuery.filters.map { when (it) {
            VideoSearchQueryFilter.EDUCATIONAL -> Filter(VideoMetadata::isEducational, true)
        }}

        val query: Query = Query.parse(videoSearchQuery.text).withFilters(filters)
        return searchService.count(query)
    }

    fun get(assetId: AssetId): Video {
        val videoAsset = videoAssetRepository
                .find(assetId) ?: throw VideoAssetNotFoundException()

        val videoPlayback = playbackRepository
                .find(videoAsset.playbackId) ?: throw VideoPlaybackNotFound()

        return Video(videoAsset, videoPlayback)
    }

    fun update(assetId: AssetId, updateCommand: VideoUpdateCommand) : Video {
        val video = get(assetId)
        val updatedVideo = updateCommand.update(video)
        val savedVideoAsset = videoAssetRepository.update(updatedVideo.asset)
        return updatedVideo.copy(asset = savedVideoAsset)
    }
}

