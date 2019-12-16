package com.boclips.contentpartner.service.domain.model

open class User(
    val id: UserId,
    val isPermittedToAccessBackoffice: Boolean,
    val context: RequestContext
)

data class RequestContext(val origin: String?)
