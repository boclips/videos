package com.boclips.contentpartner.service.infrastructure.httptrace

import org.springframework.boot.actuate.trace.http.HttpExchangeTracer
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.web.trace.servlet.HttpTraceFilter
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class TraceRequestFilter(
        repository: HttpTraceRepository,
        tracer: HttpExchangeTracer
) : HttpTraceFilter(repository, tracer) {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.servletPath?.contains("actuator") ?: false
    }
}
