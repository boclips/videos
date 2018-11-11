package com.boclips.videoanalyser.infrastructure.search

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "asset-service")
data class VideoServiceSearchProperties(var baseUrl: String = "", var username: String = "", var password: String = "") {

    fun validate(): VideoServiceSearchProperties {
        if (baseUrl.isBlank()) {
            throw IllegalStateException("Video service base URL not specified. Check application.yml")
        }
        if (username.isBlank()) {
            throw IllegalStateException("Video service username not specified. Check application.yml")
        }
        if (password.isBlank()) {
            throw IllegalStateException("Video service password not specified. Check application.yml")
        }
        return this
    }
}
