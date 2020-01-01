package com.boclips.videos.service.client.spring;

import com.boclips.videos.service.client.VideoServiceClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.util.logging.Logger;

public class VideoServiceHealthIndicator extends AbstractHealthIndicator {
    private final static Logger logger = Logger.getGlobal();

    private final VideoServiceClient videoServiceClient;

    public VideoServiceHealthIndicator(VideoServiceClient videoServiceClient) {
        this.videoServiceClient = videoServiceClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            videoServiceClient.existsByContentPartnerInfo("non-existing-providerId", "video-id");
            builder.up();
        } catch (Exception e) {
            logger.warning(e.getMessage());
            builder.down();
        }
    }
}