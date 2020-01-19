package com.boclips.videos.service.common

object Do {
    inline infix fun <reified T> exhaustive(any: T?) = any
}
