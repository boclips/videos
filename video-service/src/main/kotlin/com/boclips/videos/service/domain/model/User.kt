package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.common.UserId

open class User(
    val id: UserId,
    val isBoclipsEmployee: Boolean,
    val isAuthenticated: Boolean,
    val isPermittedToViewAnyCollection: Boolean,
    val isPermittedToRateVideos: Boolean,
    val isPermittedToUpdateVideo: Boolean,
    val isPermittedToShareVideo: Boolean,
    val context: RequestContext
)

data class RequestContext(val origin: String?)

