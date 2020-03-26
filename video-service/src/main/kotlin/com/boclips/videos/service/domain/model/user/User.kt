package com.boclips.videos.service.domain.model.user

import com.boclips.videos.service.domain.model.AccessRules

open class User(
    val id: UserId,
    val isBoclipsEmployee: Boolean,
    val isAuthenticated: Boolean,
    val isPermittedToViewAnyCollection: Boolean,
    val isPermittedToViewCollections: Boolean,
    val isPermittedToRateVideos: Boolean,
    val isPermittedToUpdateVideo: Boolean,
    val isPermittedToShareVideo: Boolean,
    val overrideIdSupplier: () -> UserId? = { null },
    val context: RequestContext,
    val accessRulesSupplier: (user: User) -> AccessRules
) {
    val accessRules: AccessRules by lazy { accessRulesSupplier(this) }
    val overrideId: UserId? by lazy { overrideIdSupplier() }

    override fun toString() = "User(id=$id)"
}

data class RequestContext(val origin: String?)

