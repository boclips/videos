package com.boclips.videos.service.domain.service.video

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.CleanUpDeactivatedVideoRequested
import com.boclips.videos.service.domain.model.collection.CollectionFilter
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.presentation.Administrator
import mu.KLogging

class VideoDuplicationService(
    private val videoRepository: VideoRepository,
    private val collectionRepository: CollectionRepository,
    private val eventBus: EventBus
) {

    companion object : KLogging()

    fun markDuplicate(
        oldVideoId: VideoId,
        activeVideoId: VideoId,
        user: User
    ) {
        videoRepository.update(
            VideoUpdateCommand.MarkAsDuplicate(videoId = oldVideoId, activeVideoId = activeVideoId)
        )

        swapVideoInCollections(oldVideoId, activeVideoId, user)
    }

    fun cleanAllDeactivatedVideos() {
        logger.info { "Starting clean up for all deactivated videos" }

        videoRepository.streamAll(VideoFilter.IsDeactivated) { deactivatedVideos ->
            deactivatedVideos.forEach {
                logger.info { "Requesting clean up for deactivated video: ${it.videoId.value}" }

                eventBus.publish(
                    CleanUpDeactivatedVideoRequested.builder().videoId(it.videoId.value).build()
                )
            }
        }
    }

    @BoclipsEventListener
    fun handleDeactivatedVideoCleanUp(request: CleanUpDeactivatedVideoRequested) {
        val oldVideoId = VideoId(request.videoId)
        logger.info { "Starting clean up for deactivated video: ${oldVideoId.value}" }

        val activeVideoId = videoRepository.find(VideoId(request.videoId))?.activeVideoId

        activeVideoId?.let {
            swapVideoInCollections(oldVideoId, activeVideoId)

        } ?: logger.info { "Clean up failed, activeVideoId missing in deactivated video: ${request.videoId}" }

    }

    private fun swapVideoInCollections(
        oldVideoId: VideoId,
        activeVideoId: VideoId,
        user: User = Administrator
    ) {
        logger.info { "Overriding deactivated video: $oldVideoId with new video: $activeVideoId in all collections" }
        collectionRepository.streamUpdate(CollectionFilter.HasVideoId(oldVideoId), { collection ->
            CollectionUpdateCommand.ReplaceVideos(
                collectionId = collection.id,
                videoIds = collection.videos.map { if (it == oldVideoId) activeVideoId else it },
                user = user
            )
        }, {})
    }
}
