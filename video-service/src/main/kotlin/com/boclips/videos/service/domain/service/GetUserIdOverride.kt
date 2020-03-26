package com.boclips.videos.service.domain.service

import com.boclips.security.utils.User
import com.boclips.videos.service.domain.model.user.UserId

interface GetUserIdOverride {
    operator fun invoke(user: User): UserId?
}
