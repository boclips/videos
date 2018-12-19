package com.boclips.videos.service.client.spring;

import com.boclips.videos.service.client.VideoServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockVideoServiceClientConfig {

    @Bean
    VideoServiceClient mockVideoServiceClient() {
        return VideoServiceClient.getFakeClient();
    }
}
