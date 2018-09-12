package com.boclips.videos.service.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "klient")
data class KalturaClientProperties(
        var partnerId: String = "",
        var userId: String = "",
        var secret: String = ""
)