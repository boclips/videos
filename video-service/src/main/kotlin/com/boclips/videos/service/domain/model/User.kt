package com.boclips.videos.service.domain.model

open class User(
    val id: UserId,
    val isBoclipsEmployee: Boolean,
    val isAuthenticated: Boolean,
    val isPermittedToViewAnyCollection: Boolean,
    val isPermittedToRateVideos: Boolean,
    val isPermittedToUpdateVideo: Boolean,
    val isPermittedToShareVideo: Boolean,
    val eventIdSupplier: () -> UserId? = { null },
    val context: RequestContext,
    val accessRulesSupplier: (user: User) -> AccessRules
) {
    val accessRules: AccessRules by lazy { accessRulesSupplier(this) }
    val eventId: UserId? by lazy { eventIdSupplier() }
}

data class RequestContext(val origin: String?)

