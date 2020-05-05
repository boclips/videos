package com.boclips.videos.service.application.video

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoBroadcastRequested
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.events.EventConverter
import mu.KLogging

class BroadcastVideos(
    private val videoRepository: VideoRepository,
    private val eventBus: EventBus
) {
    companion object : KLogging()

    operator fun invoke() {
        val batchSize = 500
        val eventConverter = EventConverter()
        videoRepository.streamAll { allVideos ->
            allVideos.windowed(size = batchSize, step = batchSize, partialWindows = true)
                .forEachIndexed { batchIndex, batchOfVideos ->
                    logger.info { "Dispatching video broadcast events: batch $batchIndex" }
                    val events = batchOfVideos.map { video ->
                        VideoBroadcastRequested(eventConverter.toVideoPayload(video))
                    }
                    eventBus.publish(events)
                }
        }
    }
}
