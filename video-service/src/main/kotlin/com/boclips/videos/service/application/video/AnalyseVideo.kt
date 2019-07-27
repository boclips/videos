package com.boclips.videos.service.application.video

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService
import mu.KLogging
import java.util.Locale

class AnalyseVideo(
    private val videoService: VideoService,
    private val eventBus: EventBus
) {
    companion object : KLogging()

    operator fun invoke(videoId: String, language: Locale?) {
        val video = videoService.getPlayableVideo(videoId = VideoId(value = videoId))
        val playback = video.playback as? VideoPlayback.StreamPlayback ?: throw VideoNotAnalysableException()

        if (video.type != LegacyVideoType.INSTRUCTIONAL_CLIPS) {
            logger.info { "Analysis of video $videoId NOT requested because its legacy type is ${video.type.name}" }
            return
        }

        if (video.playback.duration.seconds <= 20) {
            logger.info { "Analysis of video $videoId NOT requested because it's too short" }
            return
        }

        val videoToAnalyse = VideoAnalysisRequested.builder()
            .videoId(video.videoId.value)
            .videoUrl(playback.downloadUrl)
            .language(language)
            .build()

        eventBus.publish(videoToAnalyse)

        logger.info { "Analysis of video $videoId requested" }
    }
}
