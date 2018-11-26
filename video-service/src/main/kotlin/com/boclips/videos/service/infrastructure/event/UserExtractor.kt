package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.infrastructure.event.types.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal

object UserExtractor {
    fun getCurrentUser(): User {
        val user = SecurityContextHolder.getContext()?.authentication?.principal

        return when (user) {
            is Principal -> User.fromEmail(user.name)
            is UserDetails -> User.fromEmail(user.username)
            is String -> User.fromEmail(user)
            else -> User.anonymous()
        }
    }
}