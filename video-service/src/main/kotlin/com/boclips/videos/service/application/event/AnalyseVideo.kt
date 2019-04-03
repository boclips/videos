package com.boclips.videos.service.application.event

import com.boclips.events.types.VideoToAnalyse
import com.boclips.videos.service.config.messaging.Topics
import com.boclips.videos.service.domain.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.service.video.VideoService
import org.springframework.messaging.support.MessageBuilder

class AnalyseVideo(
    private val videoService: VideoService,
    private val topics: Topics
) {
    operator fun invoke(videoId: String) {
        val video = videoService.get(assetId = AssetId(value = videoId))
        val playback = video.playback as? StreamPlayback ?: throw VideoNotAnalysableException()

        val videoToAnalyse = VideoToAnalyse.builder()
            .videoId(video.asset.assetId.value)
            .videoUrl(playback.downloadUrl)
            .build()

        topics.videosToAnalyse().send(MessageBuilder.withPayload(videoToAnalyse).build())
    }
}