package com.boclips.videos.service.config.tracing

import io.opentracing.Tracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TracingContext(val tracer: Tracer) {

    @Bean
    fun mdcTraceIdAddingInterceptor() = MdcTraceIdAddingInterceptor(tracer)

    @Bean
    fun tracingTaskDecorator() = TracingTaskDecorator()
}
