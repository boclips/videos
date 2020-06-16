package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.presentation.support.Cookies
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class DeviceCookieFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        filterChain.doFilter(request, response)
        if(request.cookies.orEmpty().none { cookie -> cookie.name == Cookies.DEVICE_ID }) {
            response.addHeader(
                    "Set-Cookie",
                    "${Cookies.DEVICE_ID}=${UUID.randomUUID()}; Max-Age=31536000; Path=/; HttpOnly; SameSite=None; Secure"
            )
        }
    }
}
