package com.boclips.videos.service.application.video

import com.boclips.contentpartner.service.infrastructure.events.EventsBroadcastProperties
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoBroadcastRequested
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.events.EventConverter
import mu.KLogging

class BroadcastVideos(
    private val properties: EventsBroadcastProperties,
    private val videoRepository: VideoRepository,
    private val eventBus: EventBus
) {
    companion object : KLogging()

    operator fun invoke() {
        val batchSize = properties.videosBatchSize
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
