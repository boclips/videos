package com.boclips.contentpartner.service.infrastructure.httptrace

import org.apache.commons.collections4.queue.CircularFifoQueue
import org.springframework.boot.actuate.trace.http.HttpTrace
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Repository

@Repository
class QueueHttpTraceRepository(val properties: Properties) : HttpTraceRepository {

    private val fifo = CircularFifoQueue<HttpTrace>(properties.requestsToStore)

    override fun findAll(): List<HttpTrace> {
        return fifo.toList()
    }

    override fun add(trace: HttpTrace) {
        fifo.add(trace)
    }

    @Configuration
    @ConfigurationProperties(prefix = "httptracing")
    data class Properties (
        var requestsToStore: Int = 10
    )
}
