package com.boclips.videos.service.application.video.search

import com.boclips.events.config.Topics
import com.boclips.events.types.video.VideosExclusionFromStreamRequested
import org.springframework.integration.support.MessageBuilder

class ExcludeVideosFromSearchForStream(private val topics: Topics) {
    operator fun invoke(videoIds: List<String>) {
        val message = VideosExclusionFromStreamRequested.builder().videoIds(videoIds).build()
        topics.videosExclusionFromStreamRequested().send(MessageBuilder.withPayload(message).build())
    }
}
