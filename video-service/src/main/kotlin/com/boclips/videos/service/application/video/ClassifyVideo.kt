package com.boclips.videos.service.application.video

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoSubjectClassificationRequested
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import mu.KLogging

class ClassifyVideo(
    private val eventBus: EventBus
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

        eventBus.publish(videoSubjectClassificationRequested)
        logger.info { "Publishing subject classification requested event for video ${video.videoId.value}" }
    }
}
