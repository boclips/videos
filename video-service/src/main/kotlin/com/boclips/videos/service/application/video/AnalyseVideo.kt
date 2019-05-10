package com.boclips.videos.service.application.video

import com.boclips.events.config.Topics
import com.boclips.events.types.VideoAnalysisRequested
import com.boclips.videos.service.domain.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.playback.StreamPlayback
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
        val video = videoService.get(assetId = AssetId(value = videoId))
        val playback = video.playback as? StreamPlayback ?: throw VideoNotAnalysableException()

        if (!video.asset.searchable) {
            logger.info { "Video $videoId NOT published to ${Topics.VIDEO_ANALYSIS_REQUESTED} because it is not searchable" }
            return
        }

        if (video.asset.type != LegacyVideoType.INSTRUCTIONAL_CLIPS) {
            logger.info { "Video $videoId NOT published to ${Topics.VIDEO_ANALYSIS_REQUESTED} because its legacy type is ${video.asset.type.name}" }
            return
        }

        if (video.playback.duration.seconds <= 20) {
            logger.info { "Video $videoId NOT published to ${Topics.VIDEO_ANALYSIS_REQUESTED} because it's too short" }
            return
        }

        val videoToAnalyse = VideoAnalysisRequested.builder()
            .videoId(video.asset.assetId.value)
            .videoUrl(playback.downloadUrl)
            .language(language)
            .build()

        topics.videoAnalysisRequested().send(MessageBuilder.withPayload(videoToAnalyse).build())

        logger.info { "Video $videoId published to ${Topics.VIDEO_ANALYSIS_REQUESTED}" }
    }
}
