package com.boclips.videos.service.config.tracing

import io.opentracing.Tracer
import org.slf4j.MDC
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MdcTraceIdAddingInterceptor(private val tracer: Tracer) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val (traceId, spanId) = getTraceAndSpanId()
        MDC.put("jaeger_trace_id", traceId ?: "---")
        MDC.put("jaeger_span_id", spanId ?: "---")
        return true
    }

    private fun getTraceAndSpanId(): Pair<String?, String?> {
        return tracer.activeSpan()
            ?.context()
            ?.let { context -> context.toTraceId() to context.toSpanId() }
            ?: Pair(null, null)
    }
}
