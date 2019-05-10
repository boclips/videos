package com.boclips.videos.service.presentation.hateoas

import currentUserHasRole
import getCurrentUserIfNotAnonymous
import org.springframework.hateoas.Link

fun getIfAuthenticated(linkSupplier: (user: String) -> Link): Link? =
    getCurrentUserIfNotAnonymous()?.let { linkSupplier(it.id) }

fun getIfHasRole(role: String, linkSupplier: (user: String) -> Link): Link? =
    getIfAuthenticated(linkSupplier)?.takeIf { currentUserHasRole(role) }

fun getIfHasAnyRole(vararg roles: String, linkSupplier: (user: String) -> Link): Link? =
    roles.mapNotNull { getIfHasRole(it, linkSupplier) }.firstOrNull()
