package com.boclips.videos.service.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@ConfigurationProperties("keycloak")
@Validated
@Component
class KeycloakProperties {
    @NotBlank
    lateinit var realm: String
    @NotBlank
    lateinit var url: String
}
