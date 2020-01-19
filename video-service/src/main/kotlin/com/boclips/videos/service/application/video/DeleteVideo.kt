package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.CollectionFilter
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoSearchService
import mu.KLogging

class DeleteVideo(
    private val videoRepository: VideoRepository,
    private val collectionRepository: CollectionRepository,
    private val videoSearchService: VideoSearchService,
    private val playbackRepository: PlaybackRepository
) {
    companion object : KLogging()

    operator fun invoke(id: String?, user: User) {
        if (id == null || id.isBlank()) {
            throw VideoNotFoundException()
        }

        val videoId = VideoId(value = id)
        val video = videoRepository.find(videoId) ?: throw VideoNotFoundException(videoId)

        removeVideo(video, user)
    }

    private fun removeVideo(video: Video, user: User) {
        val videoIdToBeDeleted = video.videoId

        videoSearchService.removeFromSearch(videoIdToBeDeleted.value)
        logger.info { "Removed video $videoIdToBeDeleted from search index" }

        videoRepository.delete(videoIdToBeDeleted)
        logger.info { "Removed video $videoIdToBeDeleted from video repository" }

        collectionRepository.streamUpdate(CollectionFilter.HasVideoId(video.videoId), { collection ->
            CollectionUpdateCommand.RemoveVideoFromCollection(
                collectionId = collection.id,
                videoId = video.videoId,
                user = user
            )
        })
        logger.info { "Removed video from collections" }

        if (video.isPlayable()) {
            playbackRepository.remove(video.playback.id)
            logger.info { "Removed video $videoIdToBeDeleted from video host" }
        }
    }
}
