package com.boclips.videos.service.application.video.search

import com.boclips.events.config.Topics
import com.boclips.events.types.video.VideosInclusionInStreamRequested
import org.springframework.integration.support.MessageBuilder

class IncludeVideosInSearchForStream(private val topics: Topics) {
    operator fun invoke(videoIds: List<String>) {
        val message = VideosInclusionInStreamRequested.builder().videoIds(videoIds).build()
        topics.videosInclusionInStreamRequested().send(MessageBuilder.withPayload(message).build())
    }
}
