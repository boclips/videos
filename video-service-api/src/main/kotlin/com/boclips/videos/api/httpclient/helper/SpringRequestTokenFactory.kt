package com.boclips.videos.api.httpclient.helper

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class SpringRequestTokenFactory : TokenFactory {
    override fun getAccessToken(): String {
        RequestContextHolder.getRequestAttributes().let {
            return when (it) {
                is ServletRequestAttributes -> it.request.getHeader("Authorization").orEmpty().removePrefix("Bearer ")
                else -> ""
            }
        }
    }
}
