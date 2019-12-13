package com.boclips.videos.service.presentation

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.RequestContext
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.service.AccessRuleService

open class BaseController(private val accessRuleService: AccessRuleService) {
    fun getCurrentUser(): User {
        val userRequest = UserExtractor.getCurrentUserIfNotAnonymous()
        val referer = RefererHeaderExtractor.getReferer()

        val id = UserId(value = userRequest?.id ?: "anonymousUser")
        return User(
            id = id,
            isBoclipsEmployee = userRequest?.boclipsEmployee ?: false,
            isAuthenticated = !userRequest?.id.isNullOrBlank(),
            isPermittedToViewAnyCollection = userRequest?.hasRole(UserRoles.VIEW_ANY_COLLECTION) ?: false,
            isPermittedToRateVideos = userRequest?.hasRole(UserRoles.RATE_VIDEOS) ?: false,
            isPermittedToUpdateVideo = userRequest?.hasRole(UserRoles.UPDATE_VIDEOS) ?: false,
            isPermittedToShareVideo = userRequest?.hasRole(UserRoles.SHARE_VIDEOS) ?: false,
            context = RequestContext(origin = referer),
            accessRulesSupplier = { user ->
                if (user.isAuthenticated) accessRuleService.getRules(user) else AccessRules.anonymousAccess()
            }
        )
    }
}

object Administrator : User(
    id = UserId("admin"),
    isBoclipsEmployee = true,
    isAuthenticated = false,
    isPermittedToRateVideos = true,
    isPermittedToViewAnyCollection = true,
    isPermittedToUpdateVideo = true,
    isPermittedToShareVideo = true,
    context = RequestContext(origin = null),
    accessRulesSupplier = {
        AccessRules(
            collectionAccess = CollectionAccessRule.Everything,
            videoAccess = VideoAccessRule.Everything
        )
    }
)
