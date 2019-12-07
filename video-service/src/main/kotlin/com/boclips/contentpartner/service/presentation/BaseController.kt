package com.boclips.contentpartner.service.presentation

import com.boclips.security.utils.User
import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.common.UserId

open class BaseController {
    fun getCurrentUser() = UserExtractor.getCurrentUser() ?: User(false, "anonymousUser", emptySet())
}

