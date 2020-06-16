package com.boclips.videos.service.domain.model.user

import com.boclips.videos.service.domain.model.AccessRules

open class User(
    val id: UserId?,
    val isBoclipsEmployee: Boolean,
    val isAuthenticated: Boolean,
    val isPermittedToModifyAnyCollection: Boolean,
    val isPermittedToViewCollections: Boolean,
    val isPermittedToRateVideos: Boolean,
    val isPermittedToUpdateVideo: Boolean,
    val externalUserIdSupplier: () -> UserId? = { null },
    val context: RequestContext,
    val accessRulesSupplier: (user: User) -> AccessRules
) {
    val accessRules: AccessRules by lazy { accessRulesSupplier(this) }
    val externalUserId: UserId? by lazy { externalUserIdSupplier() }

    fun idOrThrow(): UserId = id ?: throw UserNotAuthenticatedException()

    override fun toString() = "User(id=$id)"
}

data class RequestContext(val origin: String?, val deviceId: String?)

