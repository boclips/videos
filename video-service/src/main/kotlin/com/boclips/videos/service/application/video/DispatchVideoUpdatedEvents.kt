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
        videoRepository.streamAll { videos ->
            videos.forEach { video ->
                val event = VideoUpdated.of(EventConverter().toVideoPayload(video))
                eventBus.publish(event)
            }
        }
    }
}
