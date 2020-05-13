package com.boclips.videos.config.security

import com.boclips.videos.service.config.properties.KeycloakProperties
import org.keycloak.adapters.KeycloakConfigResolver
import org.keycloak.adapters.KeycloakDeployment
import org.keycloak.adapters.spi.HttpFacade
import org.keycloak.common.enums.SslRequired
import org.keycloak.representations.adapters.config.AdapterConfig
import org.springframework.stereotype.Component

@Component
class AppKeycloakConfigResolver(private val keycloakProperties: KeycloakProperties) : KeycloakConfigResolver {
    override fun resolve(facade: HttpFacade.Request?): KeycloakDeployment =
        KeycloakDeployment().apply {
            isBearerOnly = true
            sslRequired = SslRequired.EXTERNAL
            confidentialPort = 0
            isUseResourceRoleMappings = true

            resourceName = "video-service"
            realm = keycloakProperties.realm
            setAuthServerBaseUrl(AdapterConfig().apply { authServerUrl = keycloakProperties.url })
        }
}
