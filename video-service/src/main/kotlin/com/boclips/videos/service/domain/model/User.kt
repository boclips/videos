package com.boclips.videos.service.domain.model

open class User(
    val id: UserId,
    val isBoclipsEmployee: Boolean,
    val isAuthenticated: Boolean,
    val isPermittedToViewAnyCollection: Boolean,
    val isPermittedToRateVideos: Boolean,
    val isPermittedToUpdateVideo: Boolean,
    val isPermittedToShareVideo: Boolean,
    val context: RequestContext,
    val accessRulesSupplier: (user: User) -> AccessRules
) {
    val accessRules: AccessRules by lazy { accessRulesSupplier(this) }
}

data class RequestContext(val origin: String?)

