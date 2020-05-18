package com.boclips.videos.service.config.security

import com.boclips.videos.service.config.properties.KeycloakProperties
import org.keycloak.adapters.KeycloakConfigResolver
import org.keycloak.adapters.KeycloakDeployment
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.keycloak.adapters.spi.HttpFacade
import org.keycloak.representations.adapters.config.AdapterConfig

class AppKeycloakConfigResolver(private val keycloakProperties: KeycloakProperties) : KeycloakConfigResolver {
    private var keycloakDeployment: KeycloakDeployment

    init {
        assert(keycloakProperties.realm.isNotBlank())
        assert(keycloakProperties.url.isNotBlank())

        keycloakDeployment = KeycloakDeploymentBuilder.build(
            AdapterConfig().apply {
                isBearerOnly = true
                sslRequired = "external"
                confidentialPort = 0
                isUseResourceRoleMappings = true
                resource = "video-service"
                realm = keycloakProperties.realm
                authServerUrl = keycloakProperties.url
            }
        )
    }

    override fun resolve(facade: HttpFacade.Request?): KeycloakDeployment =
        this.keycloakDeployment
}
