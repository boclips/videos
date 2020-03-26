package com.boclips.videos.service.domain.model.user

data class UserId(val value: String) {
    override fun toString(): String {
        return "[id = ${this.value}]"
    }
}
