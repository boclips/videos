package com.boclips.contentpartner.service.domain.model

import com.boclips.videos.service.domain.model.common.UserId

open class User(
    val id: UserId,
    val isPermittedToAccessBackoffice: Boolean,
    val context: RequestContext
)

data class RequestContext(val origin: String?)
