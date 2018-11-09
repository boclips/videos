package com.boclips.videos.service.domain.service

import com.boclips.search.service.domain.SearchService
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoSearchQuery
import mu.KLogging

class VideoService(
        private val videoLibrary: VideoLibrary,
        private val searchService: SearchService,
        private val playbackService: PlaybackService
) {
    companion object : KLogging()

    fun findVideosBy(query: VideoSearchQuery): List<Video> {
        val videoIds = searchService.search(query.text).map { VideoId(value = it) }
        val allVideoDetails = videoLibrary.findVideosBy(videoIds)
        val videoPlaybacks = playbackService.getPlaybacks(allVideoDetails.map { it.playbackId })
        if(videoIds.size != videoPlaybacks.size) {
            logger.warn { "Found ${videoIds.size} videos with ${videoPlaybacks.size} playbacks for query ${query.text}" }
        }

        return allVideoDetails.mapNotNull { videoDetails ->
            val videoPlayback = videoPlaybacks[videoDetails.playbackId] ?: return@mapNotNull null
            Video(videoDetails, videoPlayback)
        }
    }

    @Throws(VideoNotFoundException::class, VideoPlaybackNotFound::class)
    fun findVideoBy(videoId: VideoId): Video {
        val videoDetails = videoLibrary.findVideosBy(listOf(videoId)).firstOrNull() ?: throw VideoNotFoundException()
        val videoPlayback = playbackService.getPlayback(videoDetails.playbackId) ?: throw VideoPlaybackNotFound()
        return Video(videoDetails, videoPlayback)
    }

    fun removeVideo(video: Video) {
        searchService.removeFromSearch(video.details.videoId.value)
        logger.info { "Removed video ${video.details.videoId} from search index" }
        videoLibrary.deleteVideoBy(video.details.videoId)
        logger.info { "Removed video ${video.details.videoId} from video repository" }
        playbackService.removePlayback(video.details.playbackId)
        logger.info { "Removed video ${video.details.videoId} from video host" }
    }
}