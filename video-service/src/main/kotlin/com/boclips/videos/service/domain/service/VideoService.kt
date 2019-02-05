package com.boclips.videos.service.domain.service

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackRespository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
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
        val assetIds = searchService.search(searchRequest).map { AssetId(value = it) }

        val allVideoAssets = videoAssetRepository.findAll(assetIds = assetIds)
        val videoPlaybacks = playbackRepository.find(allVideoAssets.map { it.playbackId })

        if (assetIds.size != videoPlaybacks.size) {
            logger.warn { "Found ${assetIds.size} videos with ${videoPlaybacks.size} playbacks for query ${query.text}" }
        }

        val videos = allVideoAssets.mapNotNull { videoAsset ->
            val videoPlayback = videoPlaybacks[videoAsset.playbackId] ?: return@mapNotNull null
            Video(videoAsset, videoPlayback)
        }

        logger.info { "Returning ${videos.size} videos for query $query" }

        return videos
    }

    fun count(videoSearchQuery: VideoSearchQuery): Long {
        logger.info { "Counted videos for query $videoSearchQuery" }
        return searchService.count(videoSearchQuery.toSearchQuery())
    }

    fun get(assetId: AssetId): Video {
        val videoAsset = videoAssetRepository
                .find(assetId) ?: throw VideoAssetNotFoundException(assetId)

        val videoPlayback = playbackRepository
                .find(videoAsset.playbackId) ?: throw VideoPlaybackNotFound()

        logger.info { "Retrieved video $assetId" }

        return Video(videoAsset, videoPlayback)
    }

    fun get(assetIds: List<AssetId>): List<Video> {
        val videoAssets = videoAssetRepository.findAll(assetIds)

        if (assetIds.size != videoAssets.size) {
            logger.warn {
                val assetsNotFound = assetIds - videoAssets.map { it.assetId }
                "Some of the requested video assets could not be found. Ids found: $assetsNotFound"
            }
        }

        val playbackIds = videoAssets.map { asset -> asset.playbackId }
        val videoPlaybacks: Map<PlaybackId, VideoPlayback> = playbackRepository.find(playbackIds)

        return videoAssets.mapNotNull { videoAsset ->
            val videoPlayback = videoPlaybacks[videoAsset.playbackId]

            if (videoPlayback == null) {
                logger.warn { "Failed to find playback for video ${videoAsset.assetId}" }
                return@mapNotNull null
            }

            Video(videoAsset, videoPlayback)
        }
    }

    fun update(assetId: AssetId, updateCommand: VideoUpdateCommand): Video {
        val video = get(assetId)
        val updatedVideo = updateCommand.update(video)
        val savedVideoAsset = videoAssetRepository.update(updatedVideo.asset)

        logger.info { "Updated video $assetId" }
        return updatedVideo.copy(asset = savedVideoAsset)
    }
}

