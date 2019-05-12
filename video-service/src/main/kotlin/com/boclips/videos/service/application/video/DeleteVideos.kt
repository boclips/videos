package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.SearchService
import com.boclips.videos.service.domain.service.video.VideoService

class DeleteVideos(
    private val videoRepository: VideoRepository,
    private val searchService: SearchService,
    private val playbackRepository: PlaybackRepository
) {
    operator fun invoke(id: String?) {
        if (id == null || id.isBlank()) {
            throw VideoNotFoundException()
        }

        val videoId = VideoId(value = id)
        val video = videoRepository.find(videoId) ?: throw VideoNotFoundException(videoId)

        removeVideo(video)
    }

    private fun removeVideo(video: Video) {
        val videoIdToBeDeleted = video.videoId

        searchService.removeFromSearch(videoIdToBeDeleted.value)
        VideoService.logger.info { "Removed video $videoIdToBeDeleted from search index" }

        videoRepository.delete(videoIdToBeDeleted)
        VideoService.logger.info { "Removed video $videoIdToBeDeleted from video repository" }

        if (video.isPlayable()) {
            playbackRepository.remove(video.playback.id)
            VideoService.logger.info { "Removed video $videoIdToBeDeleted from video host" }
        }
    }
}