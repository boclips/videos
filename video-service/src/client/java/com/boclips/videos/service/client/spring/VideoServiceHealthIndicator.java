package com.boclips.videos.service.client.spring;

import com.boclips.videos.service.client.VideoServiceClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

public class VideoServiceHealthIndicator extends AbstractHealthIndicator {
    private final VideoServiceClient videoServiceClient;

    public VideoServiceHealthIndicator(VideoServiceClient videoServiceClient) {
        this.videoServiceClient = videoServiceClient;
    }


    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            videoServiceClient.existsByContentPartnerInfo("non-existing-provider", "video-id");
            builder.up();
        } catch (Exception e) {
            builder.down();
        }
    }
}