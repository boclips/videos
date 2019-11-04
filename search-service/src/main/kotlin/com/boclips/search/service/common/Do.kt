package com.boclips.search.service.common

object Do {
    inline infix fun <reified T> exhaustive(any: T?) = any
}
