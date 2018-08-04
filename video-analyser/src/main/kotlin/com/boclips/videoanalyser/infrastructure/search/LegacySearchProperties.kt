package com.boclips.videoanalyser.infrastructure.search

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "legacy-search")
data class LegacySearchProperties(var baseUrl: String = "", var token: String = "") {

    fun validate(): LegacySearchProperties {
        if (baseUrl.isBlank()) {
            throw IllegalStateException("Legacy boclips API base URL not specified. Check application.yml")
        }
        if (token.isBlank()) {
            throw IllegalStateException("Legacy boclips API token not specified. Check application.yml")
        }
        return this
    }
}
