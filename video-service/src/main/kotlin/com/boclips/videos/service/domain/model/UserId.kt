package com.boclips.videos.service.domain.model

data class UserId(val value: String) {
    override fun toString(): String {
        return "[id = ${this.value}]"
    }
}
