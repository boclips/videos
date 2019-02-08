package com.boclips.videos.service.infrastructure.event.types

data class User(
    val boclipsEmployee: Boolean,
    val id: String
) {
    companion object {
        fun anonymous() = User(boclipsEmployee = false, id = "ANONYMOUS")
        fun fromSecurityUser(user: com.boclips.security.utils.User) = User(
            boclipsEmployee = user.boclipsEmployee,
            id = user.id
        )
    }
}