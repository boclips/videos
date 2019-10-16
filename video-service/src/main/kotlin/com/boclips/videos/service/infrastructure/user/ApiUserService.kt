package com.boclips.videos.service.infrastructure.user

import com.boclips.users.client.UserServiceClient
import com.boclips.videos.service.domain.service.user.UserService
import mu.KLogging
import org.springframework.cache.annotation.Cacheable

open class ApiUserService(private val userServiceClient: UserServiceClient) : UserService {

    companion object : KLogging()

    @Cacheable("user-subjects")
    override fun getSubjectIds(userId: String): Set<String>? {
        val user = try {
            userServiceClient.findUser(userId) ?: return null
        } catch(e: Exception) {
            logger.error(e) { "Error fetching subjects for user $userId" }
            return null
        }
        return user.subjects.map { subject -> subject.id }.toSet()
    }
}
