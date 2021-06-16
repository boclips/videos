package com.boclips.videos.service.presentation.support

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpServletRequest

object DeviceIdCookieExtractor {

    fun getDeviceId(): String? {
        return RequestContextHolder.getRequestAttributes().let {
            when (it) {
                is ServletRequestAttributes -> getDeviceId(it.request)
                else -> null
            }
        }
    }

    fun getDeviceId(request: HttpServletRequest): String? {
        return request.cookies.orEmpty()
            .firstOrNull { cookie ->
                cookie.name == Cookies.DEVICE_ID
            }?.value
    }
}
