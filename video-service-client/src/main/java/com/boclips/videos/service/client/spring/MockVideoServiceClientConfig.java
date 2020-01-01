package com.boclips.videos.service.client.spring;

import com.boclips.videos.service.client.VideoServiceClient;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockVideoServiceClientConfig {

    @Bean
    @ConditionalOnMissingClass("org.mockito.Mockito")
    VideoServiceClient fakeVideoServiceClient() {
        return VideoServiceClient.getFakeClient();
    }

    @Bean
    @ConditionalOnClass(Mockito.class)
    VideoServiceClient spiedFakeVideoServiceClient() {
        return Mockito.spy(VideoServiceClient.getFakeClient());
    }
}
