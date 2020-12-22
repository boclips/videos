package com.boclips.contentpartner.service.infrastructure.httptrace

import org.springframework.boot.actuate.trace.http.HttpTrace
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentLinkedQueue

@Repository
class QueueHttpTraceRepository(val properties: Properties) : HttpTraceRepository {

    private val fifo = ConcurrentLinkedQueue<HttpTrace>()

    override fun findAll(): List<HttpTrace> {
        return fifo.toList()
    }

    override fun add(trace: HttpTrace) {
        if (fifo.size >= properties.requestsToStore) {
            (0 .. fifo.size - properties.requestsToStore).forEach { _ -> fifo.poll() }
        }
        fifo.add(trace)
    }

    @Configuration
    @ConfigurationProperties(prefix = "httptracing")
    data class Properties (
        var requestsToStore: Int = 10
    )
}
