package com.boclips.videos.service.client.spring

import com.boclips.videos.service.client.VideoServiceClient
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health

class VideoServiceHealthIndicator(val videoServiceClient: VideoServiceClient) : AbstractHealthIndicator() {
    override fun doHealthCheck(builder: Health.Builder) {
        try {
            videoServiceClient.existsByContentPartnerInfo("non-existing-provider", "video-id")
            builder.up()
        } catch (e: Exception) {
            builder.down()
        }
    }
}