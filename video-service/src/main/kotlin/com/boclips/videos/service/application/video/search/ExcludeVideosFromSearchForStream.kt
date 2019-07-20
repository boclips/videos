package com.boclips.videos.service.application.video.search

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideosExclusionFromStreamRequested

class ExcludeVideosFromSearchForStream(private val eventBus: EventBus) {
    operator fun invoke(videoIds: List<String>) {
        val message = VideosExclusionFromStreamRequested.builder().videoIds(videoIds).build()
        eventBus.publish(message)
    }
}
