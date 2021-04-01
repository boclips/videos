package com.boclips.contentpartner.service.infrastructure.events

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "events-broadcast")
data class EventsBroadcastProperties(
    var videosBatchSize: Int = 1000,
    var collectionsBatchSize: Int = 500
)
