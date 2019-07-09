package com.boclips.videos.service.application.video.search

import com.boclips.events.config.Topics
import com.boclips.events.types.video.VideosInclusionInDownloadRequested
import org.springframework.integration.support.MessageBuilder

class IncludeVideosInSearchForDownload(private val topics: Topics) {
    operator fun invoke(videoIds: List<String>) {
        val message = VideosInclusionInDownloadRequested.builder().videoIds(videoIds).build()
        topics.videosInclusionInDownloadRequested().send(MessageBuilder.withPayload(message).build())
    }
}
