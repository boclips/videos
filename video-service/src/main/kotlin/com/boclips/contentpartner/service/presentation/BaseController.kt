package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.RequestContext
import com.boclips.contentpartner.service.domain.model.User
import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.presentation.RefererHeaderExtractor

open class BaseController {
    fun getCurrentUser(): User {
        val userRequest = UserExtractor.getCurrentUser()
        val referer = RefererHeaderExtractor.getReferer()

        return User(
            id = UserId(value = userRequest?.id ?: "anonymousUser"),
            isPermittedToAccessBackoffice = userRequest?.hasRole(UserRoles.BACKOFFICE) ?: false,
            context = RequestContext(origin = referer)
        )
    }
}
