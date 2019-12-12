package com.boclips.contentpartner.service.presentation

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.RequestContext
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.presentation.RefererHeaderExtractor

open class BaseController {
    fun getCurrentUser(): User {
        val userRequest = UserExtractor.getCurrentUser()
        val referer = RefererHeaderExtractor.getReferer()

        return User(
            id = UserId(value = userRequest?.id ?: "anonymousUser"),
            isBoclipsEmployee = userRequest?.boclipsEmployee ?: false,
            isAuthenticated = userRequest?.id.let { true },
            isPermittedToAccessBackoffice = userRequest?.hasRole(UserRoles.BACKOFFICE) ?: false,
            isPermittedToViewAnyCollection = userRequest?.hasRole(UserRoles.VIEW_ANY_COLLECTION) ?: false,
            isPermittedToRateVideos = userRequest?.hasRole(UserRoles.RATE_VIDEOS) ?: false,
            isPermittedToUpdateVideo = userRequest?.hasRole(UserRoles.UPDATE_VIDEOS) ?: false,
            context = RequestContext(origin = referer)
        )
    }
}

