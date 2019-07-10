package com.boclips.videos.service.application.video

import com.boclips.events.config.Topics
import com.boclips.events.types.video.VideoAnalysisRequested
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService
import mu.KLogging
import org.springframework.messaging.support.MessageBuilder
import java.util.Locale

class AnalyseVideo(
    private val videoService: VideoService,
    private val topics: Topics
) {
    companion object : KLogging()

    operator fun invoke(videoId: String, language: Locale?) {
        val video = videoService.getPlayableVideo(videoId = VideoId(value = videoId))
        val playback = video.playback as? VideoPlayback.StreamPlayback ?: throw VideoNotAnalysableException()

        if (video.type != LegacyVideoType.INSTRUCTIONAL_CLIPS) {
            logger.info { "Video $videoId NOT published to ${Topics.VIDEO_ANALYSIS_REQUESTED} because its legacy type is ${video.type.name}" }
            return
        }

        if (video.playback.duration.seconds <= 20) {
            logger.info { "Video $videoId NOT published to ${Topics.VIDEO_ANALYSIS_REQUESTED} because it's too short" }
            return
        }

        val videoToAnalyse = VideoAnalysisRequested.builder()
            .videoId(video.videoId.value)
            .videoUrl(playback.downloadUrl)
            .language(language)
            .build()

        topics.videoAnalysisRequested().send(MessageBuilder.withPayload(videoToAnalyse).build())

        logger.info { "Video $videoId published to ${Topics.VIDEO_ANALYSIS_REQUESTED}" }
    }
}
