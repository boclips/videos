package com.boclips.videos.service.domain.model.user

import com.boclips.videos.service.domain.model.AccessRules

open class User(
    val id: UserId?,
    val isBoclipsEmployee: Boolean,
    val isAuthenticated: Boolean,
    val isPermittedToModifyAnyCollection: Boolean,
    val isPermittedToRateVideos: Boolean,
    val isPermittedToUpdateVideo: Boolean,
    val context: RequestContext,
    private val externalUserIdSupplier: () -> UserId? = { null },
    private val accessRulesSupplier: (user: User) -> AccessRules,
    private val organisationSupplier: (user: User) -> Organisation?
) {
    val isPermittedToViewPrices: Boolean by lazy { this.organisation?.hasAccessToPrices ?: true }
    val prices: Deal.Prices? by lazy { this.organisation?.deal?.prices }

    val accessRules: AccessRules by lazy { accessRulesSupplier(this) }
    val externalUserId: UserId? by lazy { externalUserIdSupplier() }
    val organisation: Organisation? by lazy { organisationSupplier(this) }

    fun idOrThrow(): UserId = id ?: throw UserNotAuthenticatedException()

    override fun toString() = "User(id=$id)"
}

data class RequestContext(val origin: String?, val deviceId: String?)
