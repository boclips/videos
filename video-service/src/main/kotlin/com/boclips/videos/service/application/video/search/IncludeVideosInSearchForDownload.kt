package com.boclips.videos.service.application.video.search

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideosInclusionInDownloadRequested

class IncludeVideosInSearchForDownload(private val eventBus: EventBus) {
    operator fun invoke(videoIds: List<String>) {
        val event = VideosInclusionInDownloadRequested.builder().videoIds(videoIds).build()

        eventBus.publish(event)
    }
}
