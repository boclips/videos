package com.boclips.videos.service.client.spring

import com.boclips.videos.service.client.ServiceCredentials
import com.boclips.videos.service.client.VideoServiceClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class VideoServiceClientConfig {

    @Value("\${videoService.baseUrl}")
    var baseUrl: String? = null

    @Value("\${videoService.accessTokenUri}")
    var accessTokenUri: String? = null

    @Value("\${videoService.clientId}")
    var clientId: String? = null

    @Value("\${videoService.clientSecret}")
    var clientSecret: String? = null

    @Bean
    fun videoServiceClient(): VideoServiceClient {
        return VideoServiceClient.getApiClient(
                baseUrl ?: throw IllegalStateException("Missing base url for video service client. Please configure video-service.base-url"),
                ServiceCredentials(
                        accessTokenUri = accessTokenUri ?: throw IllegalStateException("Missing base url for video service client. Please configure video-service.base-url"),
                        clientId = clientId ?: throw IllegalStateException("Missing clientId for video service client. Please configure video-service.client-id"),
                        clientSecret = clientSecret ?: throw IllegalStateException("Missing clientSecret for video service client. Please configure video-service.client-secret")
                )
        )
    }
}