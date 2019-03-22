package com.boclips.videos.service.config

import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.MessageChannel

interface VideosToAnalyseTopic {

    @Output(VideosToAnalyseTopic.OUTPUT)
    fun output(): MessageChannel

    companion object {
        const val OUTPUT = "videos-to-analyse"
    }
}

@Configuration
@EnableBinding(VideosToAnalyseTopic::class)
class MessagingContext
