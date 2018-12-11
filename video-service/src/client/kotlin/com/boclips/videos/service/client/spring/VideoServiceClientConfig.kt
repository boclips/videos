package com.boclips.videos.service.client.spring

import com.boclips.videos.service.client.ServiceCredentials
import com.boclips.videos.service.client.VideoServiceClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class VideoServiceClientConfig {

    @Value("\${video-service.base-url}")
    var baseUrl: String? = null

    @Value("\${video-service.access-token-uri}")
    var accessTokenUri: String? = null

    @Value("\${video-service.client-id}")
    var clientId: String? = null

    @Value("\${video-service.client-secret}")
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