package com.boclips.videos.service.application.video

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoUpdated
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.EventConverter

class DispatchVideoUpdatedEvents(
    private val videoRepository: VideoRepository,
    private val eventBus: EventBus
) {
    operator fun invoke() {
        val batchSize = 100
        val eventConverter = EventConverter()
        videoRepository.streamAll { allVideos ->
            allVideos.windowed(size = batchSize, step = batchSize, partialWindows = true).forEach { batchOfVideos ->
                batchOfVideos.forEach { video ->
                    val event = VideoUpdated.of(eventConverter.toVideoPayload(video))
                    eventBus.publish(event)
                }
            }
        }
    }
}
