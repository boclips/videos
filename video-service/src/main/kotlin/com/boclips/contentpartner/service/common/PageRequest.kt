package com.boclips.contentpartner.service.common

data class PageRequest(val size: Int, val page: Int) {
    fun getStartIndex() = page * size
}
