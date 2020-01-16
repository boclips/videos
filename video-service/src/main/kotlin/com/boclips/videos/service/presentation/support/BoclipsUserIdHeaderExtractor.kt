package com.boclips.videos.service.presentation.support

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object BoclipsUserIdHeaderExtractor {
    fun getUserId(): String? {
        return RequestContextHolder.getRequestAttributes().let {
            when (it) {
                is ServletRequestAttributes -> it.request.getHeader("Boclips-User-Id")
                else -> null
            }
        }
    }
}