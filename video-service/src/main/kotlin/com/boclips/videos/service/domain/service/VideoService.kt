package com.boclips.videos.service.domain.service

import com.boclips.search.service.domain.SearchService
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoSearchQuery
import mu.KLogging

class VideoService(
        private val videoRepository: VideoRepository,
        private val searchService: SearchService,
        private val playbackService: PlaybackService
) {
    companion object : KLogging()

    fun findVideosBy(query: VideoSearchQuery): List<Video> {
        val videoIds = searchService.search(query.text).map { VideoId(videoId = it) }
        logger.info { "Found ${videoIds.size} videos for query ${query.text}" }
        return videoRepository.findVideosBy(videoIds)
                .let(playbackService::getVideosWithPlayback)
    }

    @Throws(VideoNotFoundException::class, VideoPlaybackNotFound::class)
    fun findVideoBy(videoId: VideoId): Video {
        val video = videoRepository.findVideosBy(listOf(videoId)).firstOrNull() ?: throw VideoNotFoundException()
        return playbackService.getVideosWithPlayback(listOf(video)).firstOrNull() ?: throw VideoPlaybackNotFound()
    }

    fun removeVideo(video: Video) {
        searchService.removeFromSearch(video.videoId.videoId)
        logger.info { "Removed video ${video.videoId} from search index" }
        videoRepository.deleteVideoById(video.videoId)
        logger.info { "Removed video ${video.videoId} from video repository" }
        playbackService.removePlayback(video)
        logger.info { "Removed video ${video.videoId} from video host" }
    }
}