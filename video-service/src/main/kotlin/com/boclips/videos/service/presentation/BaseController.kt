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
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.hateoas.MediaTypes
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

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
            isPermittedToModifyAnyCollection = userRequest?.hasRole(UserRoles.VIEW_ANY_COLLECTION) ?: false,
            isPermittedToViewCollections = userRequest?.hasRole(UserRoles.VIEW_COLLECTIONS) ?: false,
            isPermittedToRateVideos = userRequest?.hasRole(UserRoles.RATE_VIDEOS) ?: false,
            isPermittedToUpdateVideo = userRequest?.hasRole(UserRoles.UPDATE_VIDEOS) ?: false,
            overrideIdSupplier = overrideIdSupplier,
            context = RequestContext(origin = referer),
            accessRulesSupplier = { user ->
                if (user.isAuthenticated) {
                    accessRuleService.getRules(user)
                } else {
                    AccessRules.anonymousAccess()
                }
            }
        )
    }

    fun halJsonCachedFor(maxAge: Long, unit: TimeUnit, obj: Any): ResponseEntity<ByteArray> =
        obj
            .run { jacksonObjectMapper().writeValueAsBytes(this) }
            .let { body ->
                ResponseEntity.ok()
                    .contentType(MediaType(MediaTypes.HAL_JSON, Charset.defaultCharset()))
                    .cacheControl(
                        CacheControl
                            .maxAge(maxAge, unit)
                            .cachePublic()
                    )
                    .contentLength(body.size.toLong())
                    .body(body)
            }
}

object Administrator : User(
    id = UserId("admin"),
    isBoclipsEmployee = true,
    isAuthenticated = false,
    isPermittedToRateVideos = true,
    isPermittedToModifyAnyCollection = true,
    isPermittedToViewCollections = true,
    isPermittedToUpdateVideo = true,
    context = RequestContext(origin = null),
    accessRulesSupplier = {
        AccessRules(
            collectionAccess = CollectionAccessRule.Everything,
            videoAccess = VideoAccess.Everything
        )
    }
)
