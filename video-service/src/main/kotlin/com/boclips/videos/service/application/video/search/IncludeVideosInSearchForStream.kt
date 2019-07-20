package com.boclips.videos.service.application.video.search

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideosInclusionInStreamRequested

class IncludeVideosInSearchForStream(private val eventBus: EventBus) {
    operator fun invoke(videoIds: List<String>) {
        val event = VideosInclusionInStreamRequested.builder().videoIds(videoIds).build()

        eventBus.publish(event)
    }
}
