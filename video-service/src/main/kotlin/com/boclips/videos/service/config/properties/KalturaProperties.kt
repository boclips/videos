package com.boclips.videos.service.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "klient")
data class KalturaProperties(
        var partnerId: String = "",
        var userId: String = "",
        var secret: String = ""
)