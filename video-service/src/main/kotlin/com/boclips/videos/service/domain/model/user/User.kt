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
    val organisationAndExternalUserIdSupplier: () -> Pair<UserId?, Organisation>? = { null },
    val context: RequestContext,
    val accessRulesSupplier: (user: User) -> AccessRules
) {
    private val organisationAndExternalUserId: Pair<UserId?, Organisation>? by lazy {
        organisationAndExternalUserIdSupplier()
    }
    val accessRules: AccessRules by lazy { accessRulesSupplier(this) }
    val externalUserId: UserId? = organisationAndExternalUserId?.first
    val organisation: Organisation? = organisationAndExternalUserId?.second

    fun idOrThrow(): UserId = id ?: throw UserNotAuthenticatedException()

    override fun toString() = "User(id=$id)"
}

data class RequestContext(val origin: String?, val deviceId: String?)
