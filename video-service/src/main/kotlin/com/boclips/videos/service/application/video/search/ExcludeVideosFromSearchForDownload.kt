package com.boclips.videos.service.application.video.search

import com.boclips.events.config.Topics
import com.boclips.events.types.video.VideosExclusionFromDownloadRequested
import org.springframework.integration.support.MessageBuilder

class ExcludeVideosFromSearchForDownload(private val topics: Topics) {
    operator fun invoke(videoIds: List<String>) {
        val message = VideosExclusionFromDownloadRequested.builder().videoIds(videoIds).build()
        topics.videosExclusionFromDownloadRequested().send(MessageBuilder.withPayload(message).build())
    }
}
