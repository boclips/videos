package com.boclips.videos.service.application

import com.boclips.security.utils.User
import com.boclips.security.utils.UserExtractor
import com.boclips.security.utils.hasRole
import com.boclips.videos.service.domain.model.common.UserId

fun getCurrentUserId() = UserId(value = getCurrentUser().id)

fun getCurrentUser() = UserExtractor.getCurrentUser() ?: User(false, "anonymous", emptySet())

fun getCurrentUserIfNotAnonymous(): User? {
    val user = UserExtractor.getCurrentUser()

    if (user?.id == "anonymousUser") {
        return null
    }

    return user
}

fun currentUserHasRole(role: String) = UserExtractor.getCurrentUser().hasRole(role)
fun currentUserHasAnyRole(vararg roles: String) = roles.any { role -> UserExtractor.getCurrentUser().hasRole(role) }
