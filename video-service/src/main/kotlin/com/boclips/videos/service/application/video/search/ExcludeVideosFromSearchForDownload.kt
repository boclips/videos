package com.boclips.videos.service.application.video.search

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideosExclusionFromDownloadRequested

class ExcludeVideosFromSearchForDownload(private val eventBus: EventBus) {
    operator fun invoke(videoIds: List<String>) {
        val message = VideosExclusionFromDownloadRequested.builder().videoIds(videoIds).build()
        eventBus.publish(message)
    }
}
