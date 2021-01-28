package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.common.model.CursorBasedIndexSearchRequest
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.PagingCursor
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoIdsWithCursor
import com.boclips.videos.service.domain.model.video.request.VideoIdsRequest
import com.boclips.videos.service.domain.model.video.request.VideoRequest
import com.boclips.videos.service.domain.model.video.request.VideoRequestPagingState
import com.boclips.videos.service.domain.service.video.VideoIndex
import com.boclips.videos.service.domain.service.video.VideoRepository
import mu.KLogging
import com.boclips.search.service.domain.common.model.PagingCursor as PagingCursorForSearch

class VideoRetrievalService(
        private val videoRepository: VideoRepository,
        private val videoIndex: VideoIndex
) {
    companion object : KLogging()

    fun getPlayableVideos(videoIds: List<VideoId>, videoAccess: VideoAccess): List<Video> {
        val orderById = videoIds.withIndex().associate { it.value to it.index }

        val results = videoIndex.search(
            PaginatedIndexSearchRequest(
                VideoIdsRequest(ids = videoIds).toSearchQuery(videoAccess),
                windowSize = videoIds.size
            )
        )

        return results.elements.map { VideoId(value = it) }
            .let { videoRepository.findAll(it) }
            .also { videos ->
                if (videoIds.size != videos.size) {
                    logger.info {
                        val videosNotFound = videoIds - videos.map { it.videoId }
                        "Some of the requested video videos could not be found. Ids not found: $videosNotFound"
                    }
                }
            }
            .filter { it.isPlayable() }
            .sortedBy { orderById[it.videoId] }
    }

    fun getPlayableVideo(videoId: VideoId, videoAccess: VideoAccess): Video {
        logger.info { "Getting playable video: $videoId, with access: $videoAccess" }

        val results = videoIndex.search(
            PaginatedIndexSearchRequest(
                query = VideoIdsRequest(ids = listOf(videoId)).toSearchQuery(videoAccess),
                windowSize = 1
            )
        )

        return results.elements
            .firstOrNull()
            ?.let {
                val video = videoRepository.find(videoId) ?: throw VideoNotFoundException(videoId)
                if (!video.isPlayable()) throw VideoPlaybackNotFound()
                logger.info { "Retrieved playable video $videoId" }
                video
            }
            ?: throw VideoNotFoundException().also {
                logger.info { "Could not find playable video $videoId with access rules $videoAccess" }
            }
    }

    fun getVideoIdsWithCursor(
        videoAccess: VideoAccess,
        pageSize: Int,
        cursor: PagingCursor? = null
    ): VideoIdsWithCursor {
        val videoRequest = VideoRequest(
                text = "",
                pageSize = pageSize,
                pagingState = VideoRequestPagingState.Cursor(cursor?.value)
        )
        val searchRequest = CursorBasedIndexSearchRequest(
            query = videoRequest.toQuery(videoAccess),
            windowSize = pageSize,
            cursor = cursor?.value?.let(::PagingCursorForSearch)
        )
        val results = videoIndex.search(searchRequest)
        return VideoIdsWithCursor(
            videoIds = results.elements.map(::VideoId),
            cursor = results.cursor?.value?.let(::PagingCursor)
        )
    }
}

