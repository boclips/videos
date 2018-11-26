package com.boclips.videos.service.testsupport

import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl

fun setSecurityContext(authenticatedUser: Any) {
    SecurityContextHolder.setContext(SecurityContextImpl(TestingAuthenticationToken(authenticatedUser, null)))
}
