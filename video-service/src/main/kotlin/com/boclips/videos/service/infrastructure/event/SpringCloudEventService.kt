package com.boclips.videos.service.infrastructure.event

import com.boclips.events.types.VideoToAnalyse
import com.boclips.videos.service.config.messaging.Topics
import com.boclips.videos.service.domain.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.service.EventService
import org.springframework.messaging.support.MessageBuilder

class SpringCloudEventService(
    private val topics: Topics
) : EventService {

    override fun analyseVideo(video: Video) {
        val playback = video.playback as? StreamPlayback ?: throw VideoNotAnalysableException()
        val videoToAnalyse = VideoToAnalyse.builder()
            .videoId(video.asset.assetId.value)
            .videoUrl(playback.downloadUrl)
            .build()

        topics.videosToAnalyse().send(MessageBuilder.withPayload(videoToAnalyse).build())
    }
}
