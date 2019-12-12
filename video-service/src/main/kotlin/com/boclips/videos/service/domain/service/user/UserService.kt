package com.boclips.videos.service.domain.service.user

interface UserService {
    fun getSubjectIds(userId: String): Set<String>?
}
