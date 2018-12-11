package com.boclips.videos.service.client.spring

import com.boclips.videos.service.client.VideoServiceClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class MockVideoServiceClientConfig {

    @Bean
    fun videoServiceClient(): VideoServiceClient {
        return VideoServiceClient.getFakeClient()
    }
}