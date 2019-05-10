package com.boclips.videos.service.presentation.hateoas

import getCurrentUserIfNotAnonymous
import org.springframework.hateoas.Link

fun addIfAuthenticated(linkSupplier: (user: String) -> Link): Link? =
    getCurrentUserIfNotAnonymous()?.let { linkSupplier(it.id) }
