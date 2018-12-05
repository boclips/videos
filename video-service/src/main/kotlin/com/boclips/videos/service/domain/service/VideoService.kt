package com.boclips.videos.service.domain.service

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.GenericSearchService
import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRespository
import com.boclips.videos.service.infrastructure.video.convertPageToIndex
import mu.KLogging

class VideoService(
        private val videoAssetRepository: VideoAssetRepository,
        private val searchService: SearchService,
        private val playbackRespository: PlaybackRespository
) {
    companion object : KLogging()

    fun search(query: VideoSearchQuery): List<Video> {
        val searchRequest = PaginatedSearchRequest(
                query = query.text,
                startIndex = convertPageToIndex(query.pageSize, query.pageIndex),
                windowSize = query.pageSize
        )
        val videoIds = searchService.search(searchRequest).map { AssetId(value = it) }

        val allVideoAssets = videoAssetRepository.findAll(videoIds)
        val videoPlaybacks = playbackRespository.find(allVideoAssets.map { it.playbackId })

        if (videoIds.size != videoPlaybacks.size) {
            logger.warn { "Found ${videoIds.size} videos with ${videoPlaybacks.size} playbacks for query ${query.text}" }
        }

        return allVideoAssets.mapNotNull { videoAsset ->
            val videoPlayback = videoPlaybacks[videoAsset.playbackId] ?: return@mapNotNull null
            Video(videoAsset, videoPlayback)
        }
    }

    fun count(videoSearchQuery: VideoSearchQuery): Long {
        return searchService.count(videoSearchQuery.text)
    }

    fun get(assetId: AssetId): Video {
        val videoAsset = videoAssetRepository
                .find(assetId) ?: throw VideoAssetNotFoundException()

        val videoPlayback = playbackRespository
                .find(videoAsset.playbackId) ?: throw VideoPlaybackNotFound()

        return Video(videoAsset, videoPlayback)
    }
}

