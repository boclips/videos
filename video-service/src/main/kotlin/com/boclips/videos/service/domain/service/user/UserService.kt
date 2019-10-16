package com.boclips.videos.service.domain.service.user

import com.boclips.security.utils.User

interface UserService {

    fun getSubjectIds(userId: String): Set<String>?
}
