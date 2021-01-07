package com.boclips.videos.service.infrastructure.user

import com.boclips.security.utils.User
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.presentation.support.BoclipsUserIdHeaderExtractor

class GetUserOrganisationAndExternalId(
    private val userService: UserService
) {
    operator fun invoke(user: User): Pair<UserId?, Organisation>? =
        userService.getOrganisationOfUser(user.id)
            ?.let { org -> Pair(extractUserIdFromHeader(org), org) }

    private fun extractUserIdFromHeader(organisation: Organisation): UserId? {
        val idFromRequestHeader = BoclipsUserIdHeaderExtractor.getUserId()
        return if(organisation.allowOverridingUserIds && idFromRequestHeader != null) {
            UserId(idFromRequestHeader)
        } else {
            null
        }
    }
}
