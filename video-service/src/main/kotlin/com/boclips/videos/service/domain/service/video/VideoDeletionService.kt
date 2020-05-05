package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.application.video.DeleteVideo
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionFilter
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.infrastructure.collection.CollectionRepository

class VideoDeletionService(
    private val videoRepository: VideoRepository,
    private val collectionRepository: CollectionRepository,
    private val videoIndex: VideoIndex,
    private val playbackRepository: PlaybackRepository
) {
    fun delete(videoId: VideoId, user: User) {
        val video = videoRepository.find(videoId) ?: throw VideoNotFoundException(videoId)

        videoIndex.removeFromSearch(videoId.value)
        DeleteVideo.logger.info { "Removed video $videoId from search index" }
        videoRepository.delete(videoId)
        DeleteVideo.logger.info { "Removed video $videoId from video repository" }
        collectionRepository.streamUpdate(CollectionFilter.HasVideoId(videoId), { collection ->
            CollectionUpdateCommand.RemoveVideoFromCollection(
                collectionId = collection.id,
                videoId = video.videoId,
                user = user
            )
        })
        DeleteVideo.logger.info { "Removed video from collections" }
        if (video.isPlayable()) {
            playbackRepository.remove(video.playback.id)
            DeleteVideo.logger.info { "Removed video $videoId from video host" }
        }
    }
}
