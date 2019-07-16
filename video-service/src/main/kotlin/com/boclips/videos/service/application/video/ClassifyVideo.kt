package com.boclips.videos.service.application.video

import com.boclips.events.config.Topics
import com.boclips.events.types.video.VideoSubjectClassificationRequested
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import mu.KLogging
import org.springframework.messaging.support.MessageBuilder

class ClassifyVideo(
    private val topics: Topics
) {
    companion object : KLogging()

    operator fun invoke(video: Video) {
        if (!video.isPlayable()) {
            logger.info("Ignoring subject classification request of video ${video.videoId.value} because it is unplayable")
            return
        }

        if (video.type == LegacyVideoType.STOCK || video.type == LegacyVideoType.NEWS) {
            logger.info { "Ignoring subject classification request of video ${video.videoId.value} because it has type ${video.type}" }
            return
        }

        val videoSubjectClassificationRequested = VideoSubjectClassificationRequested.builder()
            .videoId(video.videoId.value)
            .title(video.title)
            .description(video.description)
            .build()

        val message = MessageBuilder.withPayload(videoSubjectClassificationRequested).build()

        topics.videoSubjectClassificationRequested().send(message)
        logger.info { "Publishing subject classification requested event for video ${video.videoId.value}" }
    }
}
