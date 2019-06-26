package com.boclips.videos.service.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "pubsub.video-searchability-update")
data class PubSubVideoSearchabilityUpdateProperties(
    var batchSize: Int = 0
)