package com.boclips.videos.service.application.video

import org.springframework.scheduling.annotation.Async
import java.util.*

open class AnalyseChannels(
    private val videoAnalysisService: VideoAnalysisService
) {

    @Async
    open fun analyseVideosOfChannel(channelId: String, language: Locale?) {
        videoAnalysisService.analyseVideosOfChannel(channelId, language)
    }
}
