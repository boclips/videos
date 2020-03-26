package com.boclips.videos.service.infrastructure.user

import com.boclips.security.utils.User
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.presentation.support.BoclipsUserIdHeaderExtractor

class ApiGetUserIdOverride(
    private val userService: UserService
) : GetUserIdOverride {
    override operator fun invoke(user: User): UserId? =
        userService.getOrganisationOfUser(user.id)
            .takeIf { it?.allowOverridingUserIds ?: false }
            ?.let { BoclipsUserIdHeaderExtractor.getUserId() }
            ?.let(::UserId)
}
