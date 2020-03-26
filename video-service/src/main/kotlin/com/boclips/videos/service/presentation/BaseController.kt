package com.boclips.videos.service.presentation

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.user.RequestContext
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.presentation.support.RefererHeaderExtractor

open class BaseController(
    private val accessRuleService: AccessRuleService,
    private val getUserIdOverride: GetUserIdOverride
) {
    fun getCurrentUser(): User {
        val userRequest = UserExtractor.getCurrentUserIfNotAnonymous()
        val referer = RefererHeaderExtractor.getReferer()
        val overrideIdSupplier = {
            userRequest?.let(getUserIdOverride::invoke)
        }

        val id = UserId(
            value = userRequest?.id ?: "anonymousUser"
        )

        return User(
            id = id,
            isBoclipsEmployee = userRequest?.boclipsEmployee ?: false,
            isAuthenticated = !userRequest?.id.isNullOrBlank(),
            isPermittedToViewAnyCollection = userRequest?.hasRole(UserRoles.VIEW_ANY_COLLECTION) ?: false,
            isPermittedToViewCollections = userRequest?.hasRole(UserRoles.VIEW_COLLECTIONS) ?: false,
            isPermittedToRateVideos = userRequest?.hasRole(UserRoles.RATE_VIDEOS) ?: false,
            isPermittedToUpdateVideo = userRequest?.hasRole(UserRoles.UPDATE_VIDEOS) ?: false,
            isPermittedToShareVideo = userRequest?.hasRole(UserRoles.SHARE_VIDEOS) ?: false,
            overrideIdSupplier = overrideIdSupplier,
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
    isPermittedToViewCollections = true,
    isPermittedToUpdateVideo = true,
    isPermittedToShareVideo = true,
    context = RequestContext(origin = null),
    accessRulesSupplier = {
        AccessRules(
            collectionAccess = CollectionAccessRule.Everything,
            videoAccess = VideoAccess.Everything
        )
    }
)
