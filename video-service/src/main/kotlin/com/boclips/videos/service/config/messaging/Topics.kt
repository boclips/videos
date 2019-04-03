package com.boclips.videos.service.config.messaging

import com.boclips.events.types.Topics.VIDEOS_TO_ANALYSE_TOPIC
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel

interface Topics {

    @Output(VIDEOS_TO_ANALYSE_TOPIC)
    fun videosToAnalyse(): MessageChannel
}