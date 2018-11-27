package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.infrastructure.event.types.User
import org.keycloak.KeycloakPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal

object UserExtractor {
    fun getCurrentUser(): User {
        val user = SecurityContextHolder.getContext()?.authentication?.principal

        return when (user) {
            is KeycloakPrincipal<*> ->
                User.fromEmail(
                        email = user.keycloakSecurityContext.token.preferredUsername,
                        id = user.name
                )
            is Principal ->
                User.fromEmail(
                        email = user.name,
                        id = user.name
                )
            is UserDetails ->
                User.fromEmail(
                        email = user.username,
                        id = user.username
                )
            is String ->
                User.fromEmail(
                        email = user,
                        id = user
                )
            else ->
                User.anonymous()
        }
    }
}