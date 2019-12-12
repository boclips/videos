package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.common.UserId

open class User(
    val id: UserId,
    val isBoclipsEmployee: Boolean,
    val isAuthenticated: Boolean,
    val isAdministrator: Boolean,
    val isPermittedToViewAnyCollection: Boolean,
    val isPermittedToRateVideos: Boolean,
    val context: RequestContext,
    val isPermittedToUpdateVideo: Boolean
)

data class RequestContext(val origin: String?)

