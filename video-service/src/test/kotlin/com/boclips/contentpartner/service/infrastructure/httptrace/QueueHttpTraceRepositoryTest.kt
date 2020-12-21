package com.boclips.contentpartner.service.infrastructure.httptrace

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.trace.http.HttpTrace
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@TestPropertySource(properties = [
    "httptracing.requests-to-store=4"
])
@EnableConfigurationProperties(value = [QueueHttpTraceRepository.Properties::class])
class QueueHttpTraceRepositoryTest {

    @Autowired
    private var properties: QueueHttpTraceRepository.Properties? = null

    @Test
    fun `should store no more traces than specified as maximum`() {
        val traceRepository = QueueHttpTraceRepository(properties!!)
        (1..8).forEach { index ->
            val timeTaken = index.toLong()
            val trace = HttpTrace(null, null, null, null, null, timeTaken)
            traceRepository.add(trace)
        }

        assertThat(traceRepository.findAll().size).isEqualTo(4)
        assertThat(traceRepository.findAll().map { it.timeTaken }).isEqualTo(listOf(5L, 6L, 7L, 8L))
    }
}
