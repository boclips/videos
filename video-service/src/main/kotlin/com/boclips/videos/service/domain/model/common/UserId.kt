package com.boclips.videos.service.domain.model.common

data class UserId(val value: String) {
    override fun toString(): String {
        return "[id = ${this.value}]"
    }
}