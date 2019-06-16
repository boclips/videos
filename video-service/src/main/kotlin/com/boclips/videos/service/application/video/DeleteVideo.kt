package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.collection.CollectionsUpdateCommand
import com.boclips.videos.service.domain.service.video.VideoSearchService
import mu.KLogging

class DeleteVideo(
    private val videoRepository: VideoRepository,
    private val collectionRepository: CollectionRepository,
    private val videoSearchService: VideoSearchService,
    private val playbackRepository: PlaybackRepository
) {
    companion object : KLogging()

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

        videoSearchService.removeFromSearch(videoIdToBeDeleted.value)
        logger.info { "Removed video $videoIdToBeDeleted from search index" }

        videoRepository.delete(videoIdToBeDeleted)
        logger.info { "Removed video $videoIdToBeDeleted from video repository" }

        collectionRepository.update(CollectionsUpdateCommand.RemoveVideoFromAllCollections(videoId = video.videoId))
        logger.info { "Removed video from collections" }

        if (video.isPlayable()) {
            playbackRepository.remove(video.playback.id)
            logger.info { "Removed video $videoIdToBeDeleted from video host" }
        }
    }
}