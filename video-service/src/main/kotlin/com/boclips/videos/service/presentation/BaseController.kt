package com.boclips.videos.service.presentation

import com.boclips.security.utils.User
import com.boclips.security.utils.UserExtractor

open class BaseController {
    fun getCurrentUser() = UserExtractor.getCurrentUser() ?: User(false, "anonymousUser", emptySet())
}
