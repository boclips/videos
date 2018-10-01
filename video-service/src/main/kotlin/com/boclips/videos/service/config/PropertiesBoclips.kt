package com.boclips.videos.service.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("boclips")
@Component
data class BoclipsConfigProperties(
        var teacher: Credentials = Credentials()
)

data class Credentials(
        var username: String? = null,
        var password: String? = null
)