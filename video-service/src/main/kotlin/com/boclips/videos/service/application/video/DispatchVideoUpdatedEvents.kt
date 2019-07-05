package com.boclips.videos.service.application.video

import com.boclips.events.config.Topics
import com.boclips.events.types.video.VideoUpdated
import com.boclips.videos.service.domain.model.video.VideoRepository
import org.springframework.messaging.support.MessageBuilder

class DispatchVideoUpdatedEvents(
    private val videoRepository: VideoRepository,
    private val topics: Topics
) {

    operator fun invoke() {
        videoRepository.streamAll { videos ->
            videos.forEach { video ->
                val event = VideoUpdated.builder()
                    .videoId(video.videoId.value)
                    .title(video.title)
                    .contentPartnerName(video.contentPartner.name)
                    .build()
                topics.videoUpdated().send(MessageBuilder.withPayload(event).build())
            }
        }
    }
}
