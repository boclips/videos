package com.boclips.videos.service.application.event

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.service.EventService
import com.boclips.videos.service.domain.service.video.VideoService

class AnalyseVideo(
    private val videoService: VideoService,
    private val eventService: EventService
) {

    operator fun invoke(videoId: String) {
        val video = videoService.get(assetId = AssetId(value = videoId))

        eventService.analyseVideo(video)
    }
}