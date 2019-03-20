package com.boclips.videos.service.infrastructure.analytics

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object RefererHeaderExtractor {
    fun getReferer(): String? {
        return RequestContextHolder.getRequestAttributes().let {
            when (it) {
                is ServletRequestAttributes -> it.request.getHeader("Referer")
                else -> null
            }
        }
    }
}