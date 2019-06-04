package com.boclips.videos.service.application.video

import com.boclips.events.config.Topics
import com.boclips.events.types.video.VideoSubjectClassificationRequested
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService
import org.springframework.messaging.support.MessageBuilder

class ClassifyVideo(
    private val videoService: VideoService,
    private val topics: Topics
) {
    operator fun invoke(videoId: String) {
        val video = videoService.getPlayableVideo(videoId = VideoId(value = videoId))

        val videoSubjectClassificationRequested = VideoSubjectClassificationRequested.builder()
            .videoId(video.videoId.value)
            .title(video.title)
            .description(video.description)
            .build()

        val message = MessageBuilder.withPayload(videoSubjectClassificationRequested).build()

        topics.videoSubjectClassificationRequested().send(message)
    }
}
