package com.boclips.contentpartner.service.infrastructure.httptrace

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.actuate.trace.http.HttpExchangeTracer
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

@ExtendWith(MockitoExtension::class)
class TraceRequestFilterTest {

    @Mock
    private lateinit var repository: HttpTraceRepository

    @Mock
    private lateinit var tracer: HttpExchangeTracer

    private var filter: TraceRequestFilter? = null

    @BeforeEach
    fun init() {
        filter = TraceRequestFilter(repository, tracer)
    }

    @Test
    fun `should exclude actuator requests`() {
        val req = MockHttpServletRequest("GET", "http://localhost:8080/actuator/health")
        req.servletPath = "/actuator/health"
        val resp = MockHttpServletResponse()

        filter!!.doFilter(req, resp, MockFilterChain())

        then(repository).shouldHaveNoInteractions()
    }

    @Test
    fun `should include non-actuator requests`() {
        val req = MockHttpServletRequest("GET", "http://localhost:8080/v1/subjects")
        req.servletPath = "/v1/subjects"
        val resp = MockHttpServletResponse()

        filter!!.doFilter(req, resp, MockFilterChain())

        then(repository).should().add(Mockito.any())
    }
}
