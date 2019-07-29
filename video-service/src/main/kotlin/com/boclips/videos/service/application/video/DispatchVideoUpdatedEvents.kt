package com.boclips.videos.service.application.video

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoUpdated
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.EventConverter
import mu.KLogging

class DispatchVideoUpdatedEvents(
    private val videoRepository: VideoRepository,
    private val eventBus: EventBus
) {
    companion object: KLogging()

    operator fun invoke() {
        val batchSize = 100
        val eventConverter = EventConverter()
        videoRepository.streamAll { allVideos ->
            allVideos.windowed(size = batchSize, step = batchSize, partialWindows = true).forEachIndexed { batchIndex, batchOfVideos ->
                logger.info { "Dispatching video updated events: batch $batchIndex" }
                batchOfVideos.forEach { video ->
                    val event = VideoUpdated.of(eventConverter.toVideoPayload(video))
                    eventBus.publish(event)
                }
            }
        }
    }
}
