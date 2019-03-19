package com.boclips.videos.service.config

import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.MessageChannel

interface VideosToAnalyse {

    @Output(VideosToAnalyse.OUTPUT)
    fun output(): MessageChannel

    companion object {
        const val OUTPUT = "videos-to-analyse"
    }
}

@Configuration
@EnableBinding(VideosToAnalyse::class)
class MessagingContext
