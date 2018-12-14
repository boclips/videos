package com.boclips.videos.service.client.spring;

import com.boclips.videos.service.client.ServiceCredentials;
import com.boclips.videos.service.client.VideoServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VideoServiceClientConfig {

    @Value("${video-service.base-url}")
    public String baseUrl = null;

    @Value("${video-service.access-token-uri}")
    public String accessTokenUri = null;

    @Value("${video-service.client-id}")
    public String clientId = null;

    @Value("${video-service.client-secret}")
    public String clientSecret = null;

    @Bean
    public VideoServiceClient videoServiceClient() {
        if (baseUrl == null) {
            throw new IllegalStateException("Missing base url for video service client. Please configure video-service.base-url");
        }

        if (accessTokenUri == null) {
            throw new IllegalStateException("Missing base url for video service client. Please configure video-service.base-url");
        }

        if (clientId == null) {
            throw new IllegalStateException("Missing clientId for video service client. Please configure video-service.client-id");
        }

        if (clientSecret == null) {
            throw new IllegalStateException("Missing clientSecret for video service client. Please configure video-service.client-secret");
        }


        return VideoServiceClient.getApiClient(
                baseUrl,
                ServiceCredentials.builder()
                        .accessTokenUri(accessTokenUri)
                        .clientId(clientId)
                        .clientSecret(clientSecret).build()
        );
    }
}