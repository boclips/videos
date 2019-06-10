package com.boclips.videos.service.application.video

import com.boclips.events.config.Topics
import com.boclips.events.types.video.VideoSubjectClassificationRequested
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService
import mu.KLogging
import org.springframework.messaging.support.MessageBuilder

class ClassifyVideo(
    private val videoService: VideoService,
    private val topics: Topics
) {
    companion object : KLogging()

    operator fun invoke(videoId: String) {
        val video = try {
            videoService.getPlayableVideo(videoId = VideoId(value = videoId))
        } catch(e: Exception) {
            return
        }

        if(video.type != LegacyVideoType.INSTRUCTIONAL_CLIPS) {
            logger.info { "Ignoring subject classification request of video $videoId because it is not instructional" }
            return
        }

        val videoSubjectClassificationRequested = VideoSubjectClassificationRequested.builder()
            .videoId(video.videoId.value)
            .title(video.title)
            .description(video.description)
            .build()

        val message = MessageBuilder.withPayload(videoSubjectClassificationRequested).build()

        topics.videoSubjectClassificationRequested().send(message)
        logger.info { "Publishing subject classification requested event for video $videoId" }
    }
}
