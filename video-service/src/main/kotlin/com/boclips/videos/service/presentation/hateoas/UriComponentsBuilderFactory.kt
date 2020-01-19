package com.boclips.videos.service.presentation.hateoas

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletRequest

@Component
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class UriComponentsBuilderFactory(val request: HttpServletRequest) {
    fun getInstance(): UriComponentsBuilder = UriComponentsBuilder.fromHttpRequest(
        ServletServerHttpRequest(request)
    )
}
