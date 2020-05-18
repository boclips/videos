package com.boclips.videos.service.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("keycloak")
@Component
class KeycloakProperties {
    lateinit var realm: String
    lateinit var url: String
}
