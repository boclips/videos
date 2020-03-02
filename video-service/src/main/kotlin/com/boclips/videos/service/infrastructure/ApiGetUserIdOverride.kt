package com.boclips.videos.service.infrastructure

import com.boclips.security.utils.User
import com.boclips.users.client.UserServiceClient
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.presentation.support.BoclipsUserIdHeaderExtractor

class ApiGetUserIdOverride(private val userServiceClient: UserServiceClient) : GetUserIdOverride {
    override operator fun invoke(user: User): UserId? =
        userServiceClient.findUser(user.id)
            ?.let { userServiceClient.getOrganisation(it.organisationAccountId) }
            ?.takeIf { it.organisationDetails?.allowsOverridingUserIds ?: false }
            ?.let { BoclipsUserIdHeaderExtractor.getUserId() }
            ?.let(::UserId)
}
