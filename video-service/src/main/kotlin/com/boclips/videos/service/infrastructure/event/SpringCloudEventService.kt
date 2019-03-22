package com.boclips.videos.service.infrastructure.event

import com.boclips.eventtypes.VideoToAnalyse
import com.boclips.videos.service.config.VideosToAnalyseTopic
import com.boclips.videos.service.domain.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.service.EventService
import org.springframework.messaging.support.MessageBuilder

class SpringCloudEventService(
    private val videosToAnalyse: VideosToAnalyseTopic
) : EventService {

    override fun analyseVideo(video: Video) {
        val playback = video.playback as? StreamPlayback ?: throw VideoNotAnalysableException()
        val videoToAnalyse = VideoToAnalyse.builder()
            .videoId(video.asset.assetId.value)
            .videoUrl(playback.downloadUrl)
            .build()

        videosToAnalyse.output().send(MessageBuilder.withPayload(videoToAnalyse).build())
    }
}
