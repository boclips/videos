package com.boclips.videoanalyser.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "klient")
data class PropertiesKaltura(
        var partnerId: String = "",
        var userId: String = "",
        var secret: String = ""
)